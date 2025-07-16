package com.tjoeun.elasticsearch;

import scala.collection.Seq;
import org.openkoreantext.processor.OpenKoreanTextProcessorJava;
import org.openkoreantext.processor.tokenizer.KoreanTokenizer;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class KoreanNounExtractor {

    // 불용어 리스트 (원하는 만큼 확장 가능)
    private static final Set<String> STOPWORDS = Set.of(
            // 기본 불용어
            "이", "자", "직", "주요", "합니다", "브니다", "모집", "채용", "업무", "진행", "수행", "위해", "관련", "하",
            "경력", "경험", "관리", "직원", "요구", "내용", "사항", "지원", "능력", "수준", "정규", "사", "보수", "방식", "형태",
            "담당", "및", "제공", "가능", "우대", "자격", "조건", "근무", "근무지", "회사", "복리", "후생", "정도", "필요", "자세", "상세",
            "이상", "년", "개월", "직무", "포함", "있으며", "하고", "보유", "구성", "이하","기업", "긴급", "고객", "공고", "센터", "시스템", "구인",
            "신입", "경력직", "사무", "전문", "담당자", "담당업무", "포지션", "직종", "구분", "채용형태", "직책", "시작", "종료", "성별", "연령","유지",
            "바운드", "만수동", "접수", "님", "대표","개선", "기반", "통하",
            // 추가된 지역/고용형태
            "수원", "서울", "부산", "인천", "대구", "광주", "경기", "충남", "전북", "제주", "해외",
            "정규직", "계약직", "프리랜서", "인턴", "파트", "알바", "파견", "사원"
    ).stream().map(String::trim).map(String::toLowerCase).collect(Collectors.toSet());



    public static Set<String> extractNouns(String text) {
        CharSequence normalized = OpenKoreanTextProcessorJava.normalize(text);
        Seq<KoreanTokenizer.KoreanToken> tokens = OpenKoreanTextProcessorJava.tokenize(normalized);

        return OpenKoreanTextProcessorJava.tokensToJavaKoreanTokenList(tokens).stream()
                .filter(token -> token.getPos().toString().equals("Noun"))
                .map(token -> token.getText().toString().trim())
                .peek(raw -> {
//                    System.out.println("원본 추출된 명사: " + raw);
                    raw.codePoints().forEach(cp -> System.out.printf("  U+%04X (%c)%n", cp, cp));
                })
                .map(noun -> noun.replaceAll("[^a-zA-Z가-힣]", "").toLowerCase()) // 정규 한글만 남기기
                .peek(cleaned -> {
//                    System.out.println("정제 후 명사: " + cleaned);
                    cleaned.codePoints().forEach(cp -> System.out.printf("  ↳ U+%04X (%c)%n", cp, cp));
                })
                .filter(cleanedNoun -> {
                    if (cleanedNoun.isEmpty()) return false;
                    if (cleanedNoun.equals("ᆫ") || cleanedNoun.contains("ᆫ")) {
//                        System.out.println("'ᆫ' 직접 포함 제거됨: " + cleanedNoun);
                        return false;
                    }
                    if (cleanedNoun.matches("^[ㄱ-ㅎ]$")) {
//                        System.out.println("자음 한 글자 제거됨: " + cleanedNoun);
                        return false;
                    }
                    if (cleanedNoun.codePoints().anyMatch(c -> c >= 0x11A8 && c <= 0x11FF)) {
//                        System.out.println("종성 자모 제거됨: " + cleanedNoun);
                        return false;
                    }
                    if (cleanedNoun.length() <= 1) {
//                        System.out.println("한 글자 제거됨: " + cleanedNoun);
                        return false;
                    }
                    if (STOPWORDS.contains(cleanedNoun)) {
//                        System.out.println("불용어 제거됨: " + cleanedNoun);
                        return false;
                    }

                    if (cleanedNoun.contains("통하") || cleanedNoun.equals("통한") || cleanedNoun.equals("통해")) {
//                        System.out.println("통하/통한/통해 제거됨: " + cleanedNoun);
                        return false;
                    }


                    return true;
                })
                .collect(Collectors.toSet());

    }





}
