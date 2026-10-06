#include <DS1302.h> // DS1302 RTC 라이브러리

// 핀 설정
// [주의] VCC는 아두이노 5V, GND는 아두이노 GND 핀에 직접 연결하세요.
#define CLK 11 // Clock (SCLK) 핀
#define DAT 12 // Data (I/O) 핀
#define RST                                                                    \
  6 // Reset (CE) 핀 (13번 핀은 보드 내장 LED 간섭 때문에 6번 핀으로 변경)
#define RELAYLED 7 // 식물 성장 LED 릴레이 핀

DS1302 rtc(RST, DAT, CLK); // DS1302 객체 생성

void setup() {
  Serial.begin(9600);

  pinMode(RELAYLED, OUTPUT);
  digitalWrite(RELAYLED, HIGH); // 기본 식물 성장 LED 켬

  // [시간 설정 주의사항]
  // 최초 1회 아두이노에 업로드할 때만 아래 5줄의 주석을 해제하여 RTC 모듈의
  // 시계를 설정하세요. 시계 설정이 완료된 후에는 아래 5줄을 다시 주석 처리하고
  // 업로드해야 재부팅 시 시간 리셋을 방지할 수 있습니다.
  /*
  rtc.writeProtect(false); // 쓰기 보호 해제
  rtc.setDOW(WEDNESDAY);   // 요일 설정
  rtc.setTime(17, 7, 40);  // 시, 분, 초 설정
  rtc.setDate(5, 6, 2024); // 일, 월, 년 설정
  rtc.writeProtect(true);  // 쓰기 보호 설정
  */
}

void loop() {
  // RTC 모듈에서 현재 시간 데이터 가져오기
  Time t = rtc.getTime();
  int h = t.hour; // 현재 시(0~23)

  // 밤 22시(오후 10시)부터 아침 7시 전(06:59:59)까지 식물 성장 LED 끔
  if (h >= 22 || h < 7) {
    digitalWrite(RELAYLED, LOW); // 식물 성장 LED 끔
  } else {
    digitalWrite(RELAYLED, HIGH); // 낮 시간대 식물 성장 LED 켬
  }

  // 1초 대기
  delay(1000);
}
