import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.StringTokenizer;

/**
 * 계산기 클라이언트의 메인 클래스.
 * 사용자로부터 키보드 입력을 받아 서버로 전송하고,
 * 서버의 응답을 받아 해석한 뒤 화면에 출력.
 */
public class CalculatorClient {

    // 클라이언트 프로그램의 시작점
    public static void main(String[] args) {
        
        // 1. ConfigLoader 클래스를 이용해 'server_info.dat' 파일에서 서버 정보를 읽어옴.
        ConfigLoader config = new ConfigLoader("server_info.dat");
        
        // 2. 읽어온 IP와 포트로 서버에 접속(Socket 생성)을 시도함.
        //    try-with-resources를 사용해 소켓, 스트림, 스캐너를 자동으로 닫음.
        try (
            Socket socket = new Socket(config.getServerIp(), config.getPort());
            // 서버로부터 데이터를 읽기 위한 '입력 스트림'
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // 서버로 데이터를 보내기 위한 '출력 스트림' (autoFlush=true)
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            // 사용자로부터 키보드 입력을 받기 위한 '스캐너'
            Scanner scanner = new Scanner(System.in)
        ) {
            
            System.out.println("서버에 연결되었습니다. (" + config.getServerIp() + ":" + config.getPort() + ")");
            
            // 사용자가 'bye'를 입력할 때까지 계속 실행됨
            while (true) {
                System.out.print("계산식 (예: ADD 10 20) 또는 'bye' 입력 >> ");
                // 사용자로부터 키보드 입력을 한 줄 받음
                String outputMessage = scanner.nextLine(); 

                // 3. 사용자가 입력한 문자열을 서버로 전송함.
                //    (PrintWriter의 autoFlush=true로 설정했기 때문에 즉시 전송됨)
                out.println(outputMessage);

                // 'bye'를 입력했으면 서버에도 보냈으니, 루프를 탈출해서 프로그램을 종료함.
                if (outputMessage.equalsIgnoreCase("bye")) {
                    System.out.println("서버와의 연결을 종료합니다.");
                    break;
                }

                // 4. 서버로부터 계산 결과(응답)가 올 때까지 여기서 대기함.
                String inputMessage = in.readLine();
                
                // 5. 서버가 보낸 응답(예: "10 30")을 해석해서 화면에 출력함.
                parseResponse(inputMessage);
            }

        } catch (UnknownHostException e) {
            // IP 주소(호스트 이름)를 찾지 못했을 때 발생하는 예외
            System.out.println("오류: 서버 주소를 찾을 수 없습니다: " + config.getServerIp());
        } catch (IOException e) {
            // 서버가 꺼져있거나 네트워크 연결이 안 될 때 발생하는 예외
            System.out.println("오류: 서버에 연결할 수 없습니다: " + e.getMessage());
        }
        // try-with-resources 블록이 끝나면 소켓과 스트림이 자동으로 닫힘.
    }

    /**
     * 서버가 보낸 프로토콜 메시지(예: "10 30")를 해석해서 
     * 사용자가 이해하기 쉬운 문장으로 화면에 출력함.
     * (과제 요구사항: 프로토콜 기반 메시지 해석)
     */
    private static void parseResponse(String response) {
        // 서버가 응답 없이 연결을 끊었을 경우(null)를 대비.
        if (response == null) {
            System.out.println("서버로부터 응답이 없습니다.");
            return;
        }

        // 응답 메시지(예: "10 30")를 공백 기준으로 분리.
        StringTokenizer st = new StringTokenizer(response, " ");
        
        if (!st.hasMoreTokens()) {
            System.out.println("잘못된 서버 응답: " + response);
            return;
        }

        // 첫 번째 조각(상태 코드)을 읽어옴
        String code = st.nextToken();
        
        // 상태 코드에 따라 다른 메시지를 출력함
        switch (code) {
            case "10": // "10"은 성공 코드.
                // 두 번째 조각(결과값)을 가져와 출력함.
                System.out.println("계산 결과: " + st.nextToken());
                break;
            case "20": // "20"은 0으로 나누기 오류 코드.
                System.out.println("오류: 0으로 나눌 수 없습니다.");
                break;
            case "30": // "30"은 인자/형식 오류 코드.
                System.out.println("오류: 인자(숫자)가 잘못되었습니다. (예: ADD 10 20)");
                break;
            case "40": // "40"은 연산자 오류 코드.
                System.out.println("오류: 지원하지 않는 연산자입니다. (ADD, SUB, MUL, DIV만 가능)");
                break;
            default:
                // "10", "20", "30", "40" 외의 코드가 오면 "알 수 없는 응답"으로 처리.
                System.out.println("알 수 없는 서버 응답: " + response);
                break;
        }
    }
}