module com.tictactoe.tictactoeserver {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.tictactoe.tictactoeserver to javafx.fxml;
    exports com.tictactoe.tictactoeserver;
}