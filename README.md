# 🌱 Smart Plant System (스마트 식물 관리 시스템)

스마트 식물 관리 시스템(Smart Plant System)은 아두이노 모듈 기반의 자동화 임베디드 제어기(Smart Farm & RTC LED)와 안드로이드 애플리케이션을 연동하여 식물의 생장 환경(온도, 습도, 토양 수분, 수위, 광량)을 실시간 모니터링하고 제어하는 지능형 프로젝트입니다.

---

## 📐 시스템 아키텍처 (System Architecture)

```
[안드로이드 앱 (Android)] 
        │ 
        ▼ (HC-06 Bluetooth / Serial1)
[Arduino Mega 2560 (스마트 팜 컨트롤러)]
   ├── DHT11 온습도 센서
   ├── 토양 수분 센서 (Soil Moisture)
   ├── 수위 측정 센서 (Water Level)
   ├── 릴레이 - 워터 펌프 (Water Pump)
   ├── 릴레이 - 환기 팬 (Ventilation Fan)
   └── I2C LCD (16x2)

[Arduino Uno (LED 타이머 컨트롤러)]
   ├── DS1302 RTC (실시간 시계 모듈)
   └── 릴레이 - 식물 성장 LED (Grow Light)
```

---

## 🛠️ 주요 모듈 및 기능

### 1. `embedded/mega-smart-farm` (Arduino Mega 2560)
* **센서 모니터링**: 센서 값(토양 수분량, 수위량, 온도, 습도)을 측정하여 I2C LCD 화면에 실시간으로 표시합니다.
* **자동 펌프 제어**: 토양 수분이 40% 미만이고, 수조에 물이 10% 이상 잔여해 있을 때만 워터 펌프를 자동으로 가동합니다.
* **자동 제습/환기 제어**: 대기 습도가 65%를 초과할 경우 환기 팬을 자동으로 동작시킵니다.
* **블루투스 데이터 송신**: 안드로이드 앱으로 `토양수분,수위량,온도,습도` 포맷의 CSV 스트링 데이터를 1초 간격으로 전송합니다.

### 2. `embedded/uno-rtc-led` (Arduino Uno)
* **RTC 기반 LED 타이머**: DS1302 실시간 시계 모듈을 사용하여 주간/야간을 판별합니다.
* **식물 생장 LED 자동 제어**: 낮 시간대(07:00 ~ 21:59)에는 성장 LED를 점등하고, 야간(22:00 ~ 06:59)에는 자동 소등하여 식물의 일주기 환경을 유지합니다.

### 3. `android` (Android Application)
* **실시간 모니터링 UI**: 블루투스를 통해 아두이노 메가 보드에서 수신한 온습도, 토양 수분, 수위 데이터를 대시보드 화면에 직관적으로 시각화합니다.

---

## 🔌 하드웨어 회로도 및 핀 맵 (Pinouts)

### 1) Arduino Mega 2560 (스마트 팜)

| 부품 / 센서 | 핀 유형 | 아두이노 메가 연결 핀 | 비고 |
| :--- | :--- | :--- | :--- |
| **HC-06 블루투스 모듈** | RX / TX | **Pin 19 (RX1) / Pin 18 (TX1)** | Hardware Serial1 사용 |
| **DHT11 온습도 센서** | Digital | **Pin 5** |  |
| **워터 펌프 릴레이** | Digital | **Pin 12** | Active HIGH |
| **환기 팬 릴레이** | Digital | **Pin 13** | Active HIGH |
| **토양 수분 센서** | Analog | **Pin A1** | 0~100% 매핑 |
| **수위 측정 센서** | Analog | **Pin A0** | 0~100% 매핑 |
| **I2C LCD (16x2)** | SDA / SCL | **Pin 20 (SDA) / Pin 21 (SCL)** | I2C 주소 `0x27` |

### 2) Arduino Uno (RTC & LED)

| 부품 / 센서 | 핀 유형 | 아두이노 우노 연결 핀 | 비고 |
| :--- | :--- | :--- | :--- |
| **DS1302 RTC 모듈** | SCLK / DAT / RST | **Pin 11 / Pin 12 / Pin 6** | VCC->5V, GND->GND |
| **식물 성장 LED 릴레이**| Digital | **Pin 7** | Active HIGH |

---

## 🚀 빌드 및 실행 방법 (Build & Setup Guide)

### 1) 임베디드 (Arduino IDE)
1. **아두이노 IDE**를 실행합니다.
2. 필수 라이브러리를 설치합니다 (`Ctrl + Shift + I`):
   * `DS1302`
   * `DHT sensor library` (Adafruit)
   * `LiquidCrystal I2C` (Frank de Brabander)
3. 스케치 파일을 열어 각각 업로드합니다:
   * **Arduino Mega 2560**: `embedded/mega-smart-farm/mega-smart-farm.ino` 선택 후 업로드.
   * **Arduino Uno**: `embedded/uno-rtc-led/uno-rtc-led.ino` 선택 후 업로드.

### 2) 안드로이드 앱 (Android Studio)
1. **Android Studio**를 실행하고 `android` 폴더를 개석(Open)합니다.
2. Gradle 동기화(Sync Project with Gradle Files)가 완료될 때까지 대기합니다.
3. 테스트 안드로이드 기기(스마트폰)를 연결하고 **Run 'app'** (`Shift + F10`)을 실행합니다.

---

## 📡 블루투스 데이터 통신 규격

* **데이터 송신 포맷**: `"토양수분(%),수위량(%),온도(°C),습도(%)"`
* **예시 데이터**: `"45,80,24,55\n"` (토양 수분 45%, 수위 80%, 온도 24°C, 습도 55%)
