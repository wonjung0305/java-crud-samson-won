package com.example.attendance.util;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

// Singleton 방식
public class DBConnection {
    private static DBConnection instance;   // static으로
    private final Connection connection;   // 변경하지 못하게 final로

    // 생성자를 private으로 선언
    private DBConnection() {
        Properties props = new Properties();

        // 설정 파일 읽어오기
        try (InputStream is = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {
            if (is == null) {
                throw new RuntimeException("db.properties 파일 에러.");
            }
            props.load(is);

            String url = props.getProperty("db.url");
            String user = props.getProperty("db.user");
            String password = props.getProperty("db.password");
            String driver = props.getProperty("db.driver");

            // 매개변수로 들어온 드라이버 클래스 이름을 문자열로 받아서 메모리에 로드
            Class.forName(driver);

            // 연결
            this.connection = DriverManager.getConnection(url, user, password);

        } catch (IOException | ClassNotFoundException | SQLException e) {
            throw new RuntimeException("DB 연결 실패: " + e.getMessage(), e);
        }
    }

    // 최초 호출 시에만 실제로 연결을 생성, 이후로는 같은 인스턴스를 재사용
        // synchronized: 한 번에 한 스레드만 사용하도록
    public static synchronized DBConnection getInstance() {
        if (instance == null) {   // instance가 없을 때만(최초 호출 시)
            instance = new DBConnection();   // 인스턴스 생성
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
