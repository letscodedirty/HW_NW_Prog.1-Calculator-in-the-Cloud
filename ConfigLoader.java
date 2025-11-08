import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * 'server_info.dat' 설정 파일을 읽어오는 클래스.
 * 클라이언트가 접속해야 할 서버의 IP와 포트번호를 이 파일에서 읽어옴.
 */
public class ConfigLoader {
    
    // 파일이 없거나 잘못되었을 경우 사용할 기본 IP
    private String serverIp = "localhost"; 
    
    // 파일이 없거나 잘못되었을 경우 사용할 기본 포트임 (서버와 동일하게 9999로 설정)
    private int port = 9999; 

    /**
     * 생성자.
     * 객체가 생성될 때 바로 설정 파일을 읽도록 함.
     * @param configFilePath 읽어올 설정 파일의 경로 (예: "server_info.dat")
     */
    public ConfigLoader(String configFilePath) {
        // 파일을 열고, 다 읽으면 자동으로 닫히도록 try-with-resources 구문을 사용
        try (BufferedReader reader = new BufferedReader(new FileReader(configFilePath))) {
            
            // 파일의 첫 번째 줄을 읽어옴
            String line = reader.readLine();
            
            if (line != null) {
                // 공백을 기준으로 "localhost 9999" 같은 문자열을 분리함
                String[] parts = line.split(" ");
                
                // 정상적으로 2개(IP, 포트)로 나뉘었는지 확인
                if (parts.length == 2) {
                    this.serverIp = parts[0];
                    // 문자열로 읽은 포트번호("9999")를 정수(9999)로 변환함
                    this.port = Integer.parseInt(parts[1]);
                } else {
                    // 파일은 있으나 형식이 "IP 포트"가 아닌 경우임
                    System.out.println("설정 파일 형식이 잘못되었습니다. (예: 127.0.0.1 9999)");
                    throw new IOException("Invalid config format"); // 강제로 catch 블록으로 보냄
                }
            }
        } catch (IOException | NumberFormatException e) {
            // 아래 두 경우에 여기로 오게 됨:
            // 1. 파일을 찾지 못했한 경우(IOException)
            // 2. 포트가 숫자가 아닐 경우(NumberFormatException)
            System.out.println("'" + configFilePath + "' 파일을 찾을 수 없거나 잘못되었습니다.");
            System.out.println("기본값 (localhost:9999)으로 접속을 시도합니다.");
            // 기본값(localhost, 9999)은 이미 설정되어 있으므로 별도 처리는 필요 없다.
        }
    }

    // 외부(CalculatorClient)에서 IP를 가져갈 수 있도록 하는 메소드
    public String getServerIp() {
        return serverIp;
    }

    // 외부(CalculatorClient)에서 포트번호를 가져갈 수 있도록 하는 메소드
    public int getPort() {
        return port;
    }
}