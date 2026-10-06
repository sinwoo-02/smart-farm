# Mega 핀 맵, 블루투스 통신 규격(프로토콜) 명세

본 모듈은 **Arduino Mega 2560** 보드를 기반으로 각종 환경 센서(온습도, 토양수분, 수위)를 모니터링하고, 액추에이터(워터펌프, 환기팬, LCD)를 제어하며, **안드로이드 앱과 블루투스(HC-06) 통신**을 수행하는 스마트 팜 메인 컨트롤러입니다.

---

## 📌 Arduino Mega 2560 핀 맵 (Pinout Map)

| 연결 모듈 / 센서 | 핀 이름 | 아두이노 메가 연결 핀 | 비고 및 설명 |
| :--- | :--- | :--- | :--- |
| **HC-06 블루투스 모듈** | VCC | **5V** | 5V 전원 공급 |
| | GND | **GND** | 공통 접지 |
| | TXD | **디지털 19번 (RX1)** | Mega Hardware Serial1 RX (수신) |
| | RXD | **디지털 18번 (TX1)** | Mega Hardware Serial1 TX (송신) |
| **DHT11 온습도 센서** | DATA | **디지털 5번 (D5)** | 디지털 온습도 측정 핀 |
| **워터 펌프 릴레이** | IN (Signal) | **디지털 12번 (D12)** | Active HIGH 제어 핀 |
| **환기 팬 릴레이** | IN (Signal) | **디지털 13번 (D13)** | Active HIGH 제어 핀 |
| **토양 수분 센서** | AO (Analog) | **아날로그 A1 (A1)** | 수분 측정 핀 (390~1023 -> 100~0%) |
| **수위 측정 센서** | Signal | **아날로그 A0 (A0)** | 수조 수위 측정 핀 (0~1023 -> 0~100%) |
| **I2C LCD (16x2)** | SDA | **20번 (SDA)** | I2C 데이터 신호선 |
| | SCL | **21번 (SCL)** | I2C 클럭 신호선 (I2C 주소 `0x27`) |

> ⚠️ **블루투스 연결 주의사항**: Arduino Mega 2560 보드는 디지털 2, 3번 핀에서 `SoftwareSerial` 수신(RX)을 지원하지 않습니다. 따라서 하드웨어 시리얼 포트인 **`Serial1` (19번: RX1, 18번: TX1)**을 사용해야 블루투스 데이터 수신 및 송신이 안정적으로 구동됩니다.

---

## 📡 블루투스 통신 규격 (Bluetooth Communication Protocol)

### 1. 기본 설정 (Basic Config)
* **통신 방식**: UART Serial (HC-06)
* **통신 속도 (Baudrate)**: `9600 bps`
* **전송 주기**: `1000 ms` (1초 간격 전송)

### 2. 데이터 전송 포맷 (Mega -> 안드로이드 앱)
아두이노 메가 보드에서 안드로이드 스마트폰 앱으로 1초마다 전송하는 CSV 형태의 스트링 데이터입니다.

```
soilPercent,levelPercent,T,H\n
```

### 3. 필드 명세 (Field Specifications)

| 순서 | 필드명 (Field) | 데이터 타입 | 값 범위 | 단위 | 설명 |
| :---: | :--- | :---: | :---: | :---: | :--- |
| **1** | `soilPercent` | Integer | 0 ~ 100 | % | 토양 수분함량 (젖음: 100%, 건조: 0%) |
| **2** | `levelPercent` | Integer | 0 ~ 100 | % | 수조 수위 레벨 (물 가득: 100%, 수조 빈 상태: 0%) |
| **3** | `T` (Temperature)| Integer | 0 ~ 50 | °C | 실내/온실 섭씨 온도 (DHT11) |
| **4** | `H` (Humidity) | Integer | 20 ~ 90 | % | 실내/온실 상대 습도 (DHT11) |

### 4. 데이터 전송 예시 (Example)
```text
45,80,24,55\n
```
* **해석**: 토양 수분 `45%`, 수조 수위 `80%`, 온도 `24°C`, 습도 `55%`

### 5. 안드로이드 앱 파싱 예시 (Android Kotlin/Java)
```kotlin
// 안드로이드 앱에서 수신된 문자열 파싱 예시
val receivedData = "45,80,24,55"
val tokens = receivedData.split(",")

val soilPercent = tokens[0].toInt()  // 45
val levelPercent = tokens[1].toInt() // 80
val temp = tokens[2].toInt()         // 24
val humidity = tokens[3].toInt()     // 55
```

---

## ⚙️ 제어 임계값 및 자동화 로직

1. **워터 펌프 제어**:
   * **동작 조건**: `soilPercent < 40 && levelPercent > 10`
   * 토양 수분이 40% 미만이고, 수조에 물이 최소 10% 이상 있을 때만 펌프 가동 (`HIGH`).
   * 수조에 물이 없으면 펌프 모터 공회전 방지를 위해 펌프 OFF.

2. **환기 팬 제어**:
   * **동작 조건**: `H > 65`
   * 대기 습도가 65%를 초과할 경우 팬 가동 (`HIGH`), 그 외에는 OFF.
