// 라이브러리 가져오는 코드
#include <Arduino.h>
#include <DHT.h>
#include <LiquidCrystal_I2C.h>

// 온습도 센서 (디지털 5번 핀)
#define DHTPIN 5
#define DHTTYPE DHT11

// 블루투스 통신 핀 설정
// Arduino Mega 2560은 하드웨어 시리얼 Serial1 사용 권장 (Pin 19: RX1, Pin 18:
// TX1) HC-06 TXD -> Mega 19번(RX1), HC-06 RXD -> Mega 18번(TX1) 연결
#define BTSerial Serial1

// 팬 릴레이 (디지털 13번 핀)
#define RELAYFAN 13

// 토양 수분 센서 (아날로그 A1 핀)
#define SOIL A1

// 수위 측정 센서 (아날로그 A0 핀)
#define WATERLEVEL A0

// 워터펌프 릴레이 (디지털 12번 핀)
#define RELAYPUMP 12

// 객체 생성
DHT dht(DHTPIN, DHTTYPE);
LiquidCrystal_I2C lcd(0x27, 16, 2);

void setup() {
  // 시리얼 및 블루투스(Serial1) 통신 시작
  Serial.begin(9600);
  BTSerial.begin(9600);

  // 온습도 센서 초기화
  dht.begin();

  // LCD 초기화
  lcd.init();
  lcd.backlight();
  lcd.clear();

  // 릴레이 핀 출력 설정
  pinMode(RELAYFAN, OUTPUT);
  pinMode(RELAYPUMP, OUTPUT);

  // 펌프와 팬을 초기 상태(OFF)로 설정
  digitalWrite(RELAYPUMP, LOW);
  digitalWrite(RELAYFAN, LOW);
}

void loop() {
  // 1. 센서 값 읽기 (루프 당 1회만 측정하여 읽기 실패 방지)
  float h = dht.readHumidity();          // 습도 (%)
  float t = dht.readTemperature();       // 온도 (°C)
  int soilRaw = analogRead(SOIL);        // 토양 수분 센서 값
  int levelRaw = analogRead(WATERLEVEL); // 수위 측정 센서 값

  // DHT 센서 측정 예외 처리 (NaN 값 방지)
  if (isnan(h) || isnan(t)) {
    h = 0.0;
    t = 0.0;
  }

  // 2. 값 매핑 (0~100%)
  // 토양 수분: 젖음(390) -> 100%, 건조(1023) -> 0%
  int soilPercent = map(soilRaw, 390, 1023, 100, 0);
  soilPercent = constrain(soilPercent, 0, 100);

  // 수위 레벨: 건조(0) -> 0%, 물 가득(1023) -> 100%
  int levelPercent = map(levelRaw, 0, 1023, 0, 100);
  levelPercent = constrain(levelPercent, 0, 100);

  int H = (int)h;
  int T = (int)t;

  // 3. LCD 정보 표시 (lcd.clear() 제거로 화면 깜빡임 방지)
  lcd.setCursor(0, 0);
  lcd.print("Soil:");
  if (soilPercent < 100)
    lcd.print(" ");
  if (soilPercent < 10)
    lcd.print(" ");
  lcd.print(soilPercent);
  lcd.print("% W:");
  if (levelPercent < 100)
    lcd.print(" ");
  if (levelPercent < 10)
    lcd.print(" ");
  lcd.print(levelPercent);
  lcd.print("%");

  lcd.setCursor(0, 1);
  lcd.print("H:");
  lcd.print(H);
  lcd.print("% T:");
  lcd.print(T);
  lcd.print("C   ");

  // 4. 워터펌프 제어 로직
  // (토양 수분이 40% 미만이고, 수조에 물이 10% 이상 있을 때 펌프 가동)
  if (soilPercent < 40 && levelPercent > 10) {
    digitalWrite(RELAYPUMP, HIGH); // 펌프 ON
  } else {
    digitalWrite(RELAYPUMP, LOW); // 펌프 OFF
  }

  // 5. 환기 팬 제어 로직 (습도가 65% 초과일 때 팬 가동)
  if (H > 65) {
    digitalWrite(RELAYFAN, HIGH); // 팬 ON
  } else {
    digitalWrite(RELAYFAN, LOW); // 팬 OFF
  }

  // 6. 데이터 송신 (안드로이드 앱 통신용)
  // 데이터 포맷: "토양수분,수위량,온도,습도"
  String imf = String(soilPercent) + "," + String(levelPercent) + "," +
               String(T) + "," + String(H);
  BTSerial.println(imf);
  Serial.println(imf); // 시리얼 모니터 확인용 디버그 출력

  // 1초 대기
  delay(1000);
}
