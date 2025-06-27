package com.tjoeun.service;

import org.jsoup.Jsoup;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import com.tjoeun.entity.JobPosting;
import com.tjoeun.repository.JobPostingRepository;

@Service
public class UnifiedJobCrawlerService {

  @Autowired
  private JobPostingRepository repository;

  private final String logoSaveDir = "src/main/resources/static/images/logos/";
//  @Scheduled(cron = "0 54 11 * * *", zone = "Asia/Seoul")
  @Scheduled(fixedDelay = 10000) // 10초마다 실행
  public void runCrawler() {
    List<JobPosting> allJobs = new ArrayList<>();
    System.out.println("크롤러 실행됨 (스케줄링 시작)");

    allJobs.addAll(crawlJobKorea());
    allJobs.addAll(crawlJobPlanet());
    allJobs.addAll(crawlWanted());

    // 결측 필터링
    List<JobPosting> filteredJobs = allJobs.stream()
      .filter(job -> isNotEmpty(job.getTitle()) && isNotEmpty(job.getCompany()) && isNotEmpty(job.getResponsibilities()))
      .collect(Collectors.toList());

    // 중복 제거
    List<JobPosting> uniqueJobs = removeDuplicates(filteredJobs);

    repository.saveAll(uniqueJobs);
    System.out.println("크롤링 및 저장 완료: 총 " + uniqueJobs.size() + "건");

    List<JobPosting> cleanedJobs = preprocessJobs(uniqueJobs); // 전처리
    repository.saveAll(cleanedJobs);
  }

  private List<JobPosting> crawlJobKorea() {
    List<JobPosting> result = new ArrayList<>();
    int totalPages = 1;

    try {
      for (int page = 1; page <= totalPages; page++) {
        System.out.println("JobKorea 페이지 크롤링: " + page + " 페이지");

        String ajaxUrl = "https://www.jobkorea.co.kr/Recruit/Home/_GI_List/";
        Connection.Response response = Jsoup.connect(ajaxUrl)
          .userAgent("Mozilla/5.0")
          .header("Referer", "https://www.jobkorea.co.kr/recruit/joblist?menucode=local&localorder=1")
          .header("X-Requested-With", "XMLHttpRequest")
          .data("tabType", "local")
          .data("localorder", "1")
          .data("page", String.valueOf(page))
          .method(Connection.Method.POST)
          .timeout(10000)
          .execute();

        Document doc = Jsoup.parse(response.body());
        Elements rows = doc.select("tr.devloopArea");

        for (Element row : rows) {
          Element a = row.selectFirst("td.tplTit strong a");
          if (a == null) continue;

          String title = a.text();
          String link = "https://www.jobkorea.co.kr" + a.attr("href");

          try {
            Document detail = Jsoup.connect(link)
              .userAgent("Mozilla/5.0")
              .timeout(20000).get();

            JobPosting job = new JobPosting();
            job.setTitle(title);
            job.setCompany(getText(detail, "div.header > span.coName"));

            // 마감일 추출
            String deadlineRaw = ""; // 초기화
            Element dateDl = detail.selectFirst("dl.date");
            if (dateDl != null) {
              Elements dtList = dateDl.select("dt");
              Elements ddList = dateDl.select("dd");

              for (int i = 0; i < Math.min(dtList.size(), ddList.size()); i++) {
                String dtText = dtList.get(i).text();
                if (dtText.contains("마감일")) {
                  Element span = ddList.get(i).selectFirst("span.tahoma");
                  deadlineRaw = (span != null) ? span.text().trim() : "";
                  break;
                }
              }
            }
            job.setDeadline(parseDeadline(deadlineRaw));

            // 회사로고 저장 및 로컬 경로 설정
            Element logoEl = detail.selectFirst("img#cologo");
            String originLogoUrl = (logoEl != null) ? logoEl.absUrl("src") : "";
            String company = job.getCompany();
            String localLogoPath = saveCompanyLogo(company, originLogoUrl);
            job.setLogoUrl(localLogoPath);

            // 경력, 학력
            String career = getText(detail, "dt:contains(경력) + dd strong");
            String education = getText(detail, "dt:contains(학력) + dd strong");
            job.setQualifications((career + " " + education).trim());

            job.setEmploymentType(joinedTexts(detail, "dt:contains(고용형태) + dd li"));
            job.setSalary(getText(detail, "dt:contains(급여) + dd"));
            job.setLocation(joinedTexts(detail, "dt:contains(지역) + dd a"));

            // 우대사항
            Elements dtList = detail.select("dl.tbAdd.tbPref dt");
            Elements ddList = detail.select("dl.tbAdd.tbPref dd");
            StringBuilder preferred = new StringBuilder();
            for (int i = 0; i < Math.min(dtList.size(), ddList.size()); i++) {
              preferred.append(dtList.get(i).text()).append("\n- ").append(ddList.get(i).text()).append("\n");
            }
            job.setPreferred(preferred.toString().trim());

            // 복리후생
            Elements welfareDls = detail.select("dt:contains(복리후생) + dd dl");
            List<String> benefits = new ArrayList<>();
            for (Element dl : welfareDls) {
              String dt = getText(dl, "dt");
              String dd = getText(dl, "dd");
              benefits.add(dt + ": " + dd);
            }
            job.setBenefits(String.join(", ", benefits));

            // 주요업무
            String responsibilities = "";
            Element how = detail.selectFirst("article.artReadHow");
            if (how != null) {
              Element field = how.selectFirst("dt:contains(모집분야) + dd");
              if (field != null) {
                Elements anchors = field.select("a");
                if (!anchors.isEmpty()) {
                  responsibilities = anchors.stream()
                    .map(Element::text)
                    .collect(Collectors.joining(", "));
                } else {
                  responsibilities = field.text();
                }
              }
            }
            job.setResponsibilities(responsibilities.trim());

            result.add(job);
          } catch (Exception e) {
            System.out.println("[상세 페이지 오류] " + e.getMessage());
          }

          Thread.sleep(1500);
        }
      }
    } catch (Exception e) {
      System.out.println("[JobKorea 크롤링 실패] " + e.getMessage());
    }
    return result;
  }



  private String extractSectionText(WebDriver driver, String title) {
    try {
      WebElement h3 = driver.findElement(By.xpath("//h3[text()='" + title + "']"));
      WebElement parentBox = h3.findElement(By.xpath("./ancestor::div[contains(@class,'recruitment-detail__box')]"));
      WebElement pTag = parentBox.findElement(By.cssSelector("p.recruitment-detail__txt"));
      return pTag.getText().trim();
    } catch (Exception e) {
      return "";
    }
  }


  private List<JobPosting> crawlJobPlanet() {
    List<JobPosting> result = new ArrayList<>();
    WebDriver driver = getDriver();
    if (driver == null) return result;

    try {
      driver.get("https://www.jobplanet.co.kr/job");
      Thread.sleep(3000);

      for (int i = 0; i < 3; i++) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(1000);
      }

      List<WebElement> cards = driver.findElements(By.cssSelector("a.group.block.medium"));
      List<String> links = new ArrayList<>();
      for (WebElement card : cards) {
        String href = card.getAttribute("href");
        if (href != null && !href.isEmpty()) links.add(href);
      }

      for (int idx = 0; idx < Math.min(links.size(), 10); idx++) {
        String link = links.get(idx);
        driver.get(link);
        Thread.sleep(2000);

        Map<String, String> summary = new HashMap<>();
        List<WebElement> items = driver.findElements(By.cssSelector("dl.recruitment-summary__dl > *"));
        for (int i = 0; i + 1 < items.size(); i += 2) {
          summary.put(items.get(i).getText().trim(), items.get(i + 1).getText().trim());
        }

        JobPosting job = new JobPosting();
        job.setTitle(getText(driver, By.className("ttl")));
        job.setCompany(getText(driver, By.cssSelector("span.company_name a")));

        String originLogoUrl = getAttr(driver, By.cssSelector("div.logo img"), "src");
        String company = job.getCompany();
        String localLogoPath = saveCompanyLogo(company, originLogoUrl);
        job.setLogoUrl(localLogoPath);

        job.setEmploymentType(summary.getOrDefault("고용형태", "면접 후 결정"));
        job.setQualifications(extractSectionText(driver, "자격 요건"));
        job.setResponsibilities(extractSectionText(driver, "주요 업무"));
        job.setPreferred(extractSectionText(driver, "우대사항"));
        job.setBenefits(extractSectionText(driver, "복지 및 혜택"));

        String salary = summary.getOrDefault("급여", "회사내규에 따름").trim();
        job.setSalary(salary.isEmpty() ? "회사내규에 따름" : salary);
        job.setLocation(summary.getOrDefault("근무지역", "홈페이지 참조"));

        // 마감일 처리
        String rawDeadline = summary.getOrDefault("마감일", "");
        job.setDeadline(parseDeadline(rawDeadline));

        result.add(job);
      }
    } catch (Exception e) {
      System.out.println("[잡플래닛 오류] " + e.getMessage());
      e.printStackTrace();
    } finally {
      driver.quit();
    }

    return result;
  }







  private List<JobPosting> crawlWanted() {
    List<JobPosting> result = new ArrayList<>();
    WebDriver driver = getDriver();
    if (driver == null) return result;

    try {
      driver.get("https://www.wanted.co.kr/wdlist?country=kr&job_sort=job.latest_order&years=-1&locations=all");
      Thread.sleep(3000);
      for (int i = 0; i < 3; i++) {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        Thread.sleep(1000);
      }

      Set<String> urls = new LinkedHashSet<>();
      for (WebElement el : driver.findElements(By.tagName("a"))) {
        String href = el.getAttribute("href");
        if (href != null && href.contains("/wd/")) {
          urls.add(href);
          if (urls.size() >= 10) break;
        }
      }
      System.out.println("원티드 상세 링크 수: " + urls.size());

      for (String link : urls) {
        System.out.println("원티드 상세 페이지: " + link);
        driver.get(link);
        Thread.sleep(2000);

        try {
          WebElement more = new WebDriverWait(driver, Duration.ofSeconds(3))
            .until(ExpectedConditions.elementToBeClickable(
              By.xpath("//button[.//span[contains(text(), '상세 정보 더 보기')]]")));
          ((JavascriptExecutor) driver).executeScript("arguments[0].click();", more);
          Thread.sleep(1000);
        } catch (Exception ignored) {}

        JobPosting job = new JobPosting();
        job.setTitle(getText(driver, By.cssSelector("h1.wds-58fmok")));
        job.setCompany(getText(driver, By.cssSelector("a.JobHeader_JobHeader__Tools__Company__Link__NoBQI")));

        String originLogoUrl = getAttr(driver, By.cssSelector("div.CompanyInfo_CompanyInfo__logo__Py6Uf img"), "src");
        String company = job.getCompany();
        String localLogoPath = saveCompanyLogo(company, originLogoUrl);
        job.setLogoUrl(localLogoPath);

        job.setEmploymentType("면접 후 결정");
        job.setSalary("회사내규에 따름");
        job.setLocation(getText(driver, By.cssSelector("span.JobHeader_JobHeader__Tools__Company__Info__b9P4Y")));

        job.setQualifications(getSectionText(driver, Arrays.asList("자격요건", "지원자격")));
        job.setResponsibilities(getSectionText(driver, Arrays.asList("주요업무", "담당업무", "업무 내용")));
        job.setPreferred(getSectionText(driver, Arrays.asList("우대사항", "자격증", "필수역량")));
        job.setBenefits(getText(driver, By.cssSelector("ul.CompanyTags_CompanyTags__list__XmzkW")));

        // 마감일 처리
        String rawDeadline = getText(driver, By.cssSelector("article.JobDueTime_JobDueTime__yvhtg span"));
        job.setDeadline(parseDeadline(rawDeadline));

        result.add(job);
        System.out.println("원티드 수집: " + job.getTitle());
      }
    } catch (Exception e) {
      System.out.println("[원티드 오류] " + e.getMessage());
      e.printStackTrace();
    } finally {
      driver.quit();
    }

    System.out.println("[원티드] 최종 수집 건수: " + result.size());
    return result;
  }


  // 추가 유틸 메서드
  private String getSectionText(WebDriver d, List<String> keywords) {
    for (String kw : keywords) {
      try {
        WebElement el = d.findElement(By.xpath("//div[h3[contains(text(), '" + kw + "')]]"));
        return el.getText().trim();
      } catch (Exception ignored) {}
    }
    return "";
  }


  private List<JobPosting> removeDuplicates(List<JobPosting> jobs) {
    Map<String, JobPosting> map = new LinkedHashMap<>();
    for (JobPosting job : jobs) {
      String key = job.getTitle() + "|" + job.getCompany();
      map.putIfAbsent(key, job);
    }
    return new ArrayList<>(map.values());
  }

  // 공통 유틸
  private String getText(Element parent, String selector) {
    try {
      Element el = parent.selectFirst(selector);
      return el != null ? el.text().trim() : "";
    } catch (Exception e) {
      return "";
    }
  }

  private String getAttr(Document doc, String css, String attr) {
    Element el = doc.selectFirst(css);
    return el != null ? el.absUrl(attr) : "";
  }

  private String getText(WebDriver driver, By by) {
    try {
      return driver.findElement(by).getText().trim();
    } catch (Exception e) {
      return "";
    }
  }

  private String getAttr(WebDriver driver, By by, String attr) {
    try {
      return driver.findElement(by).getAttribute(attr);
    } catch (Exception e) {
      return "";
    }
  }

  private WebDriver getDriver() {
    try {
      System.out.println("WebDriver 시작");
      ChromeOptions options = new ChromeOptions();
      options.addArguments("--no-sandbox", "--disable-dev-shm-usage");
      WebDriver driver = new ChromeDriver(options);
      return driver;
    } catch (Exception e) {
      System.out.println("WebDriver 오류: " + e.getMessage());
      return null;
    }
  }

  private LocalDate parseDeadline(String raw) {
    if (raw == null || raw.trim().isEmpty()) return LocalDate.of(9999, 12, 31);

    raw = raw.trim();

    if (raw.contains("상시") || raw.equalsIgnoreCase("채용시 마감")) {
      return LocalDate.of(9999, 12, 31);
    }

    raw = raw.replaceAll("D-\\d+", "").trim();
    raw = raw.split(" ")[0].trim();

    try {
      return LocalDate.parse(raw, DateTimeFormatter.ofPattern("yyyy.MM.dd"));
    } catch (DateTimeParseException e) {
      System.out.println("마감일 파싱 실패: " + raw);
      return LocalDate.of(9999, 12, 31);
    }
  }

  private List<JobPosting> preprocessJobs(List<JobPosting> jobs) {
    String defaultLogo = "/images/logos/logo.jpg";
    return jobs.stream()
      // 그 외는 기존처럼 기본값 세팅
      .peek(job -> {
        if (job.getDeadline() == null) job.setDeadline(LocalDate.of(9999, 12, 31));
        if (job.getQualifications() == null || job.getQualifications().isBlank())
          job.setQualifications("경력무관");
        if (job.getLogoUrl() == null || job.getLogoUrl().isBlank())
          job.setLogoUrl(defaultLogo);
        if (job.getPreferred() == null || job.getPreferred().isBlank())
          job.setPreferred("없음");
        if (job.getBenefits() == null || job.getBenefits().isBlank())
          job.setBenefits("회사내규에 따름");
        if (job.getLocation() == null || job.getLocation().isBlank())
          job.setLocation("홈페이지 참조");
        if (job.getSalary() == null || job.getSalary().isBlank())
          job.setSalary("회사내규에 따름");
        if (job.getEmploymentType() == null || job.getEmploymentType().isBlank())
          job.setEmploymentType("면접 후 결정");
      })
      .collect(Collectors.toList());
  }

  private String joinedTexts(Element parent, String selector) {
    try {
      Elements els = parent.select(selector);
      return els.stream().map(Element::text).collect(Collectors.joining(", "));
    } catch (Exception e) {
      return "";
    }
  }

  private boolean isNotEmpty(String s) {
    return s != null && !s.trim().isEmpty();
  }

  private String saveCompanyLogo(String company, String originUrl) {
    if (company == null || company.isEmpty() || originUrl == null || originUrl.isEmpty()) {
      return ""; // 저장 불가 시 빈값 리턴
    }

    try {
      // 디렉토리 없으면 생성
      File dir = new File(logoSaveDir);
      if (!dir.exists()) dir.mkdirs();

      // 파일명: 회사명_날짜.jpg (공백이나 특수문자 제거)
      String safeCompany = company.replaceAll("[^a-zA-Z0-9가-힣]", "");
      String fileName = safeCompany + ".jpg";
      File outFile = new File(dir, fileName);

      // URL 연결
      URL url = new URL(originUrl);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestProperty("User-Agent", "Mozilla/5.0");
      conn.setConnectTimeout(5000);
      conn.setReadTimeout(5000);
      conn.connect();

      // 이미지 저장
      try (InputStream in = conn.getInputStream()) {
        Files.copy(in, outFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
      }

      // 저장된 이미지의 웹에서 접근 가능한 경로 반환
      return "/images/logos/" + fileName;

    } catch (Exception e) {
      System.out.println("[saveCompanyLogo 오류] " + e.getMessage());
      return "";
    }
  }
}
