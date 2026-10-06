package com.example.arduinoproject;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    ProgressBar pr1,pr2,pr3,pr4;
    TextView state1,state2,state3,state4,state_number1,state_number2,state_number3,state_number4;
    Button btn_state;
    //블루투스 어댑터
    private BluetoothAdapter bluetoothAdapter;
    //블루투스 디바이스 데이터 셋
    private Set<BluetoothDevice> devices;
    //블루투스 디바이스
    private BluetoothDevice bluetoothDevice;
    //블루투스 소켓
    private BluetoothSocket bluetoothSocket = null;
    //블루투스에 데이터를 출력하기 위한 출력 스트림
    private OutputStream outputStream = null;
    //블루투스에 데이터를 입력하기 위한 입력 스트림
    private InputStream inputStream = null;
    //문자열 수신에 사용되는 쓰레드
    private Thread workerThread = null;
    //수신된 문자열 저장 버퍼
    private byte[] readBuffer;
    //버퍼 내 문자 저장 위치
    private int readBufferPosition;
    //페어링 된 디바이스 수
    private int pairedDeviceCount;
    //특정 인텐트를 사용하여 액티비티를 시작
    private ActivityResultLauncher<Intent> enableBluetoothLauncher;
    //특정 권한을 요청하고 그 결과를 처리
    private ActivityResultLauncher<String> requestPermissionLauncher;

    //받은 데이터 저장
    private String[] array = {};
    //array 배열에 수신된 데이터를 각각의 num1~num4에 저장
    private int num1, num2, num3, num4;


    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //ID 활성화
        pr1 = findViewById(R.id.pr1);
        pr2 = findViewById(R.id.pr2);
        pr3 = findViewById(R.id.pr3);
        pr4 = findViewById(R.id.pr4);
        state1 = findViewById(R.id.state1);
        state2 = findViewById(R.id.state2);
        state3 = findViewById(R.id.state3);
        state4 = findViewById(R.id.state4);
        state_number1 = findViewById(R.id.state_number1);
        state_number2 = findViewById(R.id.state_number2);
        state_number3 = findViewById(R.id.state_number3);
        state_number4 = findViewById(R.id.state_number4);
        btn_state = findViewById(R.id.btn_state);

        btn_state.setOnClickListener(v->{
            //받은 데이터를 저장하는 배열에 데이터가 있다면 실행되는 코드
            if (array.length > 0 && array[0] != null && !array[0].isEmpty()) {
                try {
                    //각각의 변수에 수신된 데이터를 문자열 타입에서 정수 타입으로 변환
                    num1 = Integer.parseInt(array[0]);
                    num2 = Integer.parseInt(array[1]);
                    num3 = Integer.parseInt(array[2]);
                    num4 = Integer.parseInt(array[3]);
                    String data1 = array[0]+"%";
                    String data2 = array[1]+"%";
                    String data3 = array[2]+"ºC";
                    String data4 = array[3]+"%";
                    pr1.setProgress(num1);
                    pr2.setProgress(num2);
                    pr3.setProgress(num3);
                    pr4.setProgress(num4);
                    runOnUiThread(() -> state_number1.setText(data1));
                    runOnUiThread(() -> state_number2.setText(data2));
                    runOnUiThread(() -> state_number3.setText(data3));
                    runOnUiThread(() -> state_number4.setText(data4));
                    //토양 내 수분함량을 나타내는 코드
                    if(num1>=80){
                        state1.setText("상태: 좋음");
                    }
                    else if(num1>=60){
                        state1.setText("상태: 보통");
                    }
                    else{
                        state1.setText("상태: 나쁨");
                    }
                    //물 잔량을 나타내는 코드
                    if(num2>20){
                        state2.setText("상태: 좋음");
                    }
                    else{
                        state2.setText("상태: 나쁨");
                    }
                    //온도의 상태를 나타내는 코드
                    if(num3>=18&&num3<=24){
                        state3.setText("상태: 좋음");
                    }
                    else if((num3>=16&&num3<18)||(num3>24&&num3<=26)){
                        state3.setText("상태: 보통");
                    }
                    else{
                        state3.setText("상태: 나쁨");
                    }
                    //습도의 상태를 나타내는 코드
                    if(num4>=40&&num4<=60){
                        state4.setText("상태: 좋음");
                    }
                    else if((num4>=35&&num4<40)||(num4>60&&num4<=65)){
                        state4.setText("상태: 보통");
                    }
                    else{
                        state4.setText("상태: 나쁨");
                    }
                } catch (NumberFormatException e) {
                    //받은 데이터가 숫자가 아닐 경우
                    Toast.makeText(MainActivity.this, "받은 값이 숫자가 아닙니다.", Toast.LENGTH_SHORT).show();
                }
            }
            //받은 데이터가 없는 경우
            else {
                Toast.makeText(MainActivity.this, "받은 값이 비어 있습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        //블루투스 어댑터를 디폴트 어댑터로 설정함
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        //블루투스 활성화를 확인하고 활성화 되어있다면 블루투스 기기를 선택하는 메서드를 호출함
        enableBluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        selectBluetoothDevice();
                    }
                    //블루투스가 활성화되지 않았을 경우
                    else {
                        Toast.makeText(this, "블루투스를 사용할 수 없음", Toast.LENGTH_SHORT).show();
                        finish();
                    }
                }
        );

        //권한이 부여 여부를 확인하는 코드
        requestPermissionLauncher = registerForActivityResult(
                new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    //권한이 부여된 경우
                    if (isGranted) {
                        checkBluetoothState();
                    }
                    //권한이 부여되지 않은 경우
                    else {
                        Toast.makeText(this, "권한 거부", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        checkBluetoothState();
    }

    //블루투스 상태 확인
    private void checkBluetoothState() {
        //블루투스가 없을 경우
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "블루투스를 지원하지 않음", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT);
            return;
        }
        //블루투스 어댑터를 사용할 수 있다면 selectBluetoothDevice() 메서드를 호출
        if (bluetoothAdapter.isEnabled()) {
            selectBluetoothDevice();
        } else {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetoothLauncher.launch(enableBtIntent);
        }
    }

    //블루투스 디바이스를 선택하는 메서드
    @SuppressLint("MissingPermission")
    private void selectBluetoothDevice() {
        //이미 페어링 되어있는 블루투스 기기를 탐색
        devices = bluetoothAdapter.getBondedDevices();
        //페어링 된 디바이스 크기 저장
        pairedDeviceCount = devices.size();
        //페어링 된 장치가 없는 경우
        if (pairedDeviceCount == 0) {
            Toast.makeText(this, "페어링 된 기기를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return;
        }
        //디바이스를 선택하기 위한 대화상자 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("블루투스 기기 선택");
        //페어링 된 각각의 디바이스의 이름과 주소를 저장
        List<String> list = new ArrayList<>();
        //모든 디바이스의 이름을 리스트에 추가
        for (BluetoothDevice device : devices) {
            list.add(device.getName());
        }
        list.add("취소");

        final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);
        //해당 항목을 눌렀을 때 호출되는 이벤트 리스너
        builder.setItems(charSequences, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //해당 디바이스와 연결하는 함수 호출
                if (!charSequences[which].equals("취소")) {
                    connectDevice(charSequences[which].toString());
                }
            }
        });
        //취소 버튼 누를 때 앱이 안닫히도록 설정
        builder.setCancelable(false);
        //대화상자를 생성하고 보여주는 코드
        builder.create().show();
    }

    private void connectDevice(String deviceName) {
        //페어링 된 디바이스 모두 탐색
        for (BluetoothDevice device : devices) {
            //manifest 파일에 BLUETOOTH_CONNECT 권한이 있는지 확인하는 코드
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(android.Manifest.permission.BLUETOOTH_CONNECT);
                return;
            }
            //사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (deviceName.equals(device.getName())) {
                bluetoothDevice = device;
                break;
            }
        }

        Toast.makeText(this, bluetoothDevice.getName() + " 연결됨", Toast.LENGTH_SHORT).show();
        //UUID 생성(네트워크 상에서 고유성이 보장되는 id를 만들기 위한 표준 규약)
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");
        try {
            //Rfcomm 채널에서 UUID 통해 블루투스 디바이스와 통신하는 소켓생성
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();

            //블루투스 소켓 출력 스트림을 가져옴
            outputStream = bluetoothSocket.getOutputStream();
            //블루투스 소켓 입력 스트림을 가져옴
            inputStream = bluetoothSocket.getInputStream();
            receiveData();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //데이터 수신을 위한 메서드
    private void receiveData() {
        //안드로이드에서는 UI 쓰레드가 메인 쓰레드로 동작하게되기 때문에 일반적으로 Handler를 사용하여 이를 다루게됨
        final Handler handler = new Handler(Looper.getMainLooper());
        //데이터 수신을 위한 버퍼 생성
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        //데이터 수신을 위한 스레드 생성
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                //현재 실행되는 쓰레드가 예외가 발생한다면 while문이 종료됨
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        //데이터 수신 확인(입력 스트림으로 수신된 데이터의 개수를 저장)
                        int bytesAvailable = inputStream.available();
                        //데이터 수신된 경우
                        if (bytesAvailable > 0) {
                            //수신한 데이터 수 만큼의 공간을 가지는 packetBytes 배열을 생성
                            byte[] packetBytes = new byte[bytesAvailable];
                            inputStream.read(packetBytes);
                            //입력 스트림 바이트를 한 바이트씩 읽어옴
                            for (int i = 0; i < bytesAvailable; i++) {
                                byte b = packetBytes[i];
                                //개행 문자를 기준으로 받음(한줄)
                                if (b == '\n') {
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    //배열 복사를 수행 encodedBytes 배열을 readBuffer 배열로 복사함
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    //encodedBytes에 저장된 데이터를 UTF-8형식으로 가져와 data 변수에 저장함
                                    final String data = new String(encodedBytes, "UTF-8");
                                    readBufferPosition = 0;
                                    //수분 함량, 물 잔량, 온도, 습도를 ","로 분리하여 array 배열에 저장함
                                    array = data.trim().split(",",4);

                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            //원래는 토스트 객체 받는 곳
                                        }
                                    });
                                }
                                //개행 문자가 아닌 경우
                                else {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //30초마다 데이터를 받음
                    try {
                        Thread.sleep(30000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        //스레드 시작
        workerThread.start();
    }
}