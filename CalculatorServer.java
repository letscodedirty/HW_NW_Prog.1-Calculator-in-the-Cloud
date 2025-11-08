import java.io.*;
import java.net.*;
import java.util.StringTokenizer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 계산기 서버의 메인 클래스.
 * 스레드 풀을 사용하여 동시에 여러 클라이언트의 접속을 받아
 * 계산 요청을 처리하는 기능을 함.
 */
public class CalculatorServer {

    // 서버 프로그램의 시작점
    public static void main(String[] args) throws Exception {
        
        // 서버 소켓(ServerSocket)을 9999 포트로 열음. 
        // try-with-resources 구문을 사용해 프로그램 종료 시 자동으로 소켓을 닫음.
        try (ServerSocket listener = new ServerSocket(9999)) {
            
            System.out.println("계산기 서버가 9999 포트에서 실행을 시작합니다...");
            
            // 동시에 20개의 클라이언트를 처리할 수 있는 스레드 풀을 생성.
            // (과제 요구사항: 다중 클라이언트 처리)
            ExecutorService pool = Executors.newFixedThreadPool(20);

            // 서버를 종료하지 않고 계속 실행하기 위한 무한 루프.
            while (true) {
                // 클라이언트의 접속 요청이 올 때까지 여기서 실행이 멈추고 대기.
                // 접속 요청이 오면, 서버는 클라이언트와 통신할 '소켓'을 반환.
                Socket socket = listener.accept();
                
                // 새로 생성된 소켓을 'CalculatorTask'라는 작업(Runnable)에 넘겨줌.
                // 스레드 풀의 남는 스레드가 이 작업을 맡아서 실행.
                // 메인 스레드는 다시 while 루프로 돌아가 다음 접속을 기다림.
                pool.execute(new CalculatorTask(socket));
            }
        }
    }

    /**
     * 각 클라이언트의 실제 계산 처리를 담당하는 작업 클래스.
     * Runnable 인터페이스를 구현하여 스레드 풀에서 실행될 수 있음.
     */
    private static class CalculatorTask implements Runnable {
        private Socket socket; // 클라이언트와 통신할 소켓

        // 생성자. 통신할 소켓을 받아 멤버 변수에 저장.
        CalculatorTask(Socket socket) {
            this.socket = socket;
        }

        // 스레드가 실제로 실행하는 메인 로직임.
        @Override
        public void run() {
            System.out.println("클라이언트가 연결되었습니다: " + socket);
            try (
                // 클라이언트로부터 데이터를 읽기 위한 '입력 스트림'을 생성.
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                
                // 클라이언트에게 데이터를 보내기 위한 '출력 스트림'을 생성.
                // true 옵션은 println 호출 시 자동으로 flush(데이터 즉시 전송)하게 함.
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
            ) {
                
                String line;
                // 클라이언트로부터 한 줄의 메시지가 올 때까지 대기.
                // 클라이언트가 "bye"를 보내거나 접속을 끊으면(null) 루프가 종료됨.
                while ((line = in.readLine()) != null) {
                    
                    // 클라이언트가 "bye"를 입력하면 루프를 탈출함.
                    if ("bye".equalsIgnoreCase(line)) {
                        break;
                    }
                    
                    System.out.println("수신: " + line);
                    
                    // 핵심 로직. 클라이언트가 보낸 문자열을 calculate 메소드로 넘겨 계산함.
                    String result = calculate(line);
                    
                    // 계산 결과를 클라이언트에게 다시 보냄 (송신).
                    out.println(result);
                    System.out.println("송신: " + result);
                }
                
            } catch (IOException e) {
                System.out.println("클라이언트 처리 중 오류 발생: " + socket + " " + e.getMessage());
            } finally {
                try {
                    // 통신이 끝나면(루프 탈출 or 오류) 반드시 소켓을 닫아 자원을 해제함.
                    socket.close();
                } catch (IOException e) {
                    // 소켓 닫기 실패 시 처리 (보통은 무시)
                }
                System.out.println("클라이언트 연결이 종료되었습니다: " + socket);
            }
        }

        /**
         * 문자열로 된 수식을 받아 계산하고, 결과를 프로토콜 문자열로 반환함.
         * (과제 요구사항: 4칙 연산 및 예외 처리)
         */
        private String calculate(String expression) {
            // 클라이언트와 약속한 프로토콜(통신 규칙) 정의
            // 성공: "10 [결과값]"
            // 오류 (0으로 나눔): "20"
            // 오류 (인자/형식): "30"
            // 오류 (연산자): "40"

            // "ADD 10 20" 같은 문자열을 공백 기준으로 3조각(ADD, 10, 20)으로 나누기.
            StringTokenizer st = new StringTokenizer(expression, " ");

            // 조각이 3개가 아니면 "인자 개수 오류"로 판단.
            // (과제 예시: too many arguments)
            if (st.countTokens() != 3) {
                return "30"; 
            }

            try {
                // 문자열 조각을 각각 명령어와 숫자로 분리함.
                String command = st.nextToken().toUpperCase(); // ADD, SUB...
                int op1 = Integer.parseInt(st.nextToken());    // 10
                int op2 = Integer.parseInt(st.nextToken());    // 20

                // 첫 번째 조각(명령어)에 따라 4칙 연산을 수행.
                switch (command) {
                    case "ADD":
                        return "10 " + (op1 + op2);
                    case "SUB":
                        return "10 " + (op1 - op2);
                    case "MUL":
                        return "10 " + (op1 * op2);
                    case "DIV":
                        // 나눗셈(DIV)의 경우, 0으로 나누는지 추가로 확인.
                        // (과제 예시: divided by zero)
                        if (op2 == 0) {
                            return "20"; 
                        }
                        return "10 " + (op1 / op2);
                    default:
                        // ADD, SUB, MUL, DIV가 아닌 "MIN" 같은 명령어가 오면 "알 수 없는 연산자"로 처리.
                        // (과제 예시: MIN 5 2 1 -> too many arguments, 혹은 알 수 없는 연산자)
                        // 여기서는 3조각이지만 연산자가 틀린 경우.
                        return "40"; 
                }
            } catch (NumberFormatException e) {
                // "ADD ten 20"처럼 숫자가 아닌 문자가 오면 이 예외가 발생.
                // "인자 형식 오류"로 판단.
                return "30"; 
            }
        }
    }
}