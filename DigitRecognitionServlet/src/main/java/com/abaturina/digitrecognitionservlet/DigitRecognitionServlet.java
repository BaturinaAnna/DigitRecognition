package com.abaturina.digitrecognitionservlet;

import org.apache.commons.io.FileUtils;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet(name = "digitRecognitionServlet", value = "/digit-recognition-servlet")
public class DigitRecognitionServlet extends HttpServlet {

    private final String digitPath=
            "C:\\Users\\Batur\\HSE\\digitClassificationFolder\\digit.jpg";
    private final String answerPath=
            "C:\\Users\\Batur\\HSE\\digitClassificationFolder\\answer\\answer.txt";
    private final ProcessBuilder processBuilderDigitRecognition = new ProcessBuilder(
            "python",
            "C:\\Users\\Batur\\HSE\\digitClassification\\RunModel\\venv\\run.py"
    ).inheritIO();

    public void init() {
    }

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doPost(request, response);
    }

    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("START RECOGNITION...");
        System.out.println(response);
        cleanOutputDirectory();
        getDigit(request);
        runModel();
        String answer = getAnswerFromFile();
        sendResponse(answer, response);
    }

    private void getDigit(HttpServletRequest request) throws IOException {
        System.out.println("GET DIGIT IMAGE");
        InputStream is = request.getInputStream();
        byte[] buffer = new byte[1024];
        FileOutputStream out = new FileOutputStream(digitPath);
        int read;
        while ((read = is.read(buffer)) > 0) {
            out.write(buffer, 0, read);
        }
        is.close();
        out.close();
        System.out.println("DIGIT IMAGE SAVED");
    }

    private void cleanOutputDirectory() throws IOException {
        System.out.println("START CLEANING");
        FileUtils.deleteDirectory(new File("C:\\Users\\Batur\\HSE\\digitClassificationFolder\\answer"));
        System.out.println("FINISH CLEANING");
    }

    private void runModel(){
        try {
            System.out.println("START RECOGNITION");
            Process p = processBuilderDigitRecognition.start();
            p.waitFor();
            BufferedReader bfr = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line = "";
            while ((line = bfr.readLine()) != null) {
                System.out.println(line);
            }
            p.waitFor();
            p.destroy();
            System.out.println("FINISH RECOGNITION");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    private String getAnswerFromFile() throws IOException {
        System.out.println("GET ANSWER FROM FILE");
        return Files.readAllLines(Path.of(answerPath)).get(0);
    }

    private void sendResponse(String answer, HttpServletResponse response) throws IOException {
        System.out.println("START SENDING RESPONSE");
        response.setStatus(200);
        response.setContentType("text/plain");
        PrintWriter writer = response.getWriter();
        writer.append(answer);
        System.out.println(answer);
        writer.flush();
        System.out.println("END SENDING RESPONSE");
    }

    public void destroy() {
    }
}