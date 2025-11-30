import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

public class Main extends Application {

    private RedBlackTree rbt = new RedBlackTree();
    private AVLTree avl = new AVLTree();

    private TreeCanvas rbtCanvas;
    private TreeCanvas avlCanvas;

    private Label rbtInfo;
    private Label avlInfo;

    private TreeCompare comparator = new TreeCompare();

    public static void main(String[] args) { // точка входа
        launch();

    }

    @Override
    public void start(Stage stage) { //
        stage.setTitle("Красно-черное дерево VS AVL-дерево");

        TabPane tabs = new TabPane();

        Tab tabRBT = new Tab("Красно-черное дерево", createRBTPane()); // вкладки
        Tab tabAVL = new Tab("AVL-дерево", createAVLPane());
        Tab tabCompare = new Tab("Сравнение", createComparisonPane());

        tabRBT.setClosable(false);
        tabAVL.setClosable(false);
        tabCompare.setClosable(false);

        tabs.getTabs().addAll(tabRBT, tabAVL, tabCompare);

        Scene scene = new Scene(tabs, 1100, 700);
        stage.setScene(scene);
        stage.show();

        redrawAll();
    }

    private BorderPane createRBTPane() {
        BorderPane root = new BorderPane();
        rbtCanvas = new TreeCanvas(true); // холст для дерева
        rbtInfo = new Label(); // для статы

        TextField input = new TextField();
        input.setPromptText("Число");
        Button insertBtn = new Button("Вставить");
        Button deleteBtn = new Button("Удалить");
        Button searchBtn = new Button("Найти");
        Button clearBtn = new Button("Очистить");
        HBox controls = new HBox(8, new Label("Значение:"), input, insertBtn, deleteBtn, searchBtn, clearBtn);
        controls.setPadding(new Insets(8));

        // обработчики событий
        insertBtn.setOnAction(e -> {
            try {
                int k = Integer.parseInt(input.getText().trim());
                rbt.insert(k);
                redrawAll();
            } catch (NumberFormatException ex) { showAlert("Введите целое число"); }
        });

        deleteBtn.setOnAction(e -> {
            try {
                int k = Integer.parseInt(input.getText().trim());
                rbt.delete(k);
                redrawAll();
            } catch (NumberFormatException ex) { showAlert("Введите целое число"); }
        });

        searchBtn.setOnAction(e -> {
            try {
                int k = Integer.parseInt(input.getText().trim());
                boolean found = rbt.contains(k);

                List<Integer> path = rbt.findPath(k); // путь поиска
                String pathStr = path.stream()
                        .map(String::valueOf) // преобразуем путь в строку
                        .reduce((a, b) -> a + " → " + b)
                        .orElse("отсутствует");

                showAlert("Поиск числа " + k + ": " + (found ? "найдено" : "не найдено") +
                        "\nНайденный путь: " + pathStr);
            } catch (NumberFormatException ex) { showAlert("Введите целое число"); }
        });

        clearBtn.setOnAction(e -> {
            rbt = new RedBlackTree();
            redrawAll();
        });

        VBox top = new VBox(4, controls, rbtInfo);
        root.setTop(top);
        root.setCenter(rbtCanvas);

        return root;
    }

    private BorderPane createAVLPane() { // то же самое для avl
        BorderPane root = new BorderPane();
        avlCanvas = new TreeCanvas(false);
        avlInfo = new Label();

        TextField input = new TextField();
        input.setPromptText("Число");
        Button insertBtn = new Button("Вставить");
        Button deleteBtn = new Button("Удалить");
        Button searchBtn = new Button("Найти");
        Button clearBtn = new Button("Очистить");
        HBox controls = new HBox(8, new Label("Значение:"), input, insertBtn, deleteBtn, searchBtn, clearBtn);
        controls.setPadding(new Insets(8));

        insertBtn.setOnAction(e -> {
            try {
                int k = Integer.parseInt(input.getText().trim());
                avl.insert(k);
                redrawAll();
            } catch (NumberFormatException ex) { showAlert("Введите целое число"); }
        });

        deleteBtn.setOnAction(e -> {
            try {
                int k = Integer.parseInt(input.getText().trim());
                avl.delete(k);
                redrawAll();
            } catch (NumberFormatException ex) { showAlert("Введите целое число"); }
        });

        searchBtn.setOnAction(e -> {
            try {
                int k = Integer.parseInt(input.getText().trim());
                boolean found = avl.contains(k);

                // ПОЛУЧАЕМ ПУТЬ ПОИСКА
                List<Integer> path = avl.findPath(k);
                String pathStr = path.stream()
                        .map(String::valueOf)
                        .reduce((a, b) -> a + " → " + b)
                        .orElse("отсутствует");

                showAlert("Поиск числа " + k + ": " + (found ? "найдено" : "не найдено") +
                        "\nНайденный путь: " + pathStr);
            } catch (NumberFormatException ex) { showAlert("Введите целое число"); }
        });

        clearBtn.setOnAction(e -> {
            avl = new AVLTree();
            redrawAll();
        });

        VBox top = new VBox(4, controls, avlInfo);
        root.setTop(top);
        root.setCenter(avlCanvas);

        return root;
    }

    private VBox createComparisonPane() { // панель сравнения
        VBox root = new VBox(8);
        root.setPadding(new Insets(88));

        TextField nField = new TextField("1000"); // кол-во операций
        nField.setPrefWidth(120); // ширина поля
        ChoiceBox<Integer> repeats = new ChoiceBox<>(); // выпадающий список повторений
        repeats.getItems().addAll(1,3,5);
        repeats.setValue(1);

        Button runInsertBtn = new Button("Запуск теста производительности вставки");
        Button runMixedBtn = new Button("Запуск теста смешанных операций");
        TextArea out = new TextArea(); // вывод результатов
        out.setEditable(false);
        out.setPrefRowCount(20);

        runInsertBtn.setOnAction(e -> {
            int N;
            try {
                N = Integer.parseInt(nField.getText().trim());
                if (N > 10000) {
                    if (!showConfirmAlert("N > 10000 может занять много времени. Продолжить?")) {
                        return;
                    }
                }
            } catch (NumberFormatException ex) {
                showAlert("Введите корректное N");
                return;
            }
            int rep = repeats.getValue();
            out.clear();
            out.appendText("Тест производительности вставки\n");

            TreeCompare.ComparisonResult result = comparator.compareInsert(N, rep);

            out.appendText("Повторений: " + rep + ", N=" + N + "\n");
            out.appendText(String.format("Среднее время вставки RBT (мс)= %.3f\n", result.rbtTime));
            out.appendText(String.format("Среднее время вставки AVL (мс)= %.3f\n", result.avlTime));
            out.appendText(String.format("Среднее кол-во поворотов RBT= %.1f\n", result.rbtRotations));
            out.appendText(String.format("Среднее кол-во поворотов AVL= %.1f\n", result.avlRotations));
            out.appendText(String.format("Средняя высота RBT= %.1f\n", result.rbtHeight));
            out.appendText(String.format("Средняя высота AVL= %.1f\n", result.avlHeight));
            out.appendText(String.format("Соотношение времени (AVL/RBT)= %.3f\n", result.timeRatio));
        });

        runMixedBtn.setOnAction(e -> {
            int N;
            try {
                N = Integer.parseInt(nField.getText().trim());
                if (N > 10000) {
                    if (!showConfirmAlert("N > 10000 может занять много времени. Продолжить?")) {
                        return;
                    }
                }
            } catch (NumberFormatException ex) {
                showAlert("Введите корректное N");
                return;
            }
            int rep = repeats.getValue();
            out.clear();
            out.appendText("Сравнение смешанных операций \n");
            out.appendText("Операции: 40% вставка, 30% удаление, 30% поиск\n");

            TreeCompare.ComparisonResult result = comparator.compareMixedOperations(N, rep);

            out.appendText("Повторений: " + rep + ", Кол-во узлов: " + N + "\n");
            out.appendText(String.format("Общее среднее время RBT (мс)= %.3f\n", result.rbtTime));
            out.appendText(String.format("Общее среднее время AVL (мс)= %.3f\n", result.avlTime));
            out.appendText(String.format("Среднее кол-во поворотов RBT= %.1f\n", result.rbtRotations));
            out.appendText(String.format("Среднее кол-во поворотов AVL= %.1f\n", result.avlRotations));
            out.appendText(String.format("Средняя высота RBT= %.1f\n", result.rbtHeight));
            out.appendText(String.format("Средняя высота AVL= %.1f\n", result.avlHeight));
            out.appendText(String.format("Соотношение времени (AVL/RBT)= %.3f\n", result.timeRatio));
        });

        HBox controls = new HBox(8,
                new Label("Количество узлов N:"), nField,
                new Label("Повторений:"), repeats);
        HBox buttons = new HBox(8, runInsertBtn, runMixedBtn);
        VBox controlBox = new VBox(8, controls, buttons);
        root.getChildren().addAll(controlBox, out);
        return root;
    }

    private void redrawAll() { // обновляет все визуальные компоненты
        try {
            rbtCanvas.setTreeData(rbt);
            avlCanvas.setTreeData(avl);

            rbtInfo.setText(String.format("Узлов: %d, Высота: %d, Поворотов: %d",
                    rbt.getNodeCount(), rbt.getHeight(), rbt.getRotations()));
            avlInfo.setText(String.format("Узлов: %d, Высота: %d, Поворотов: %d",
                    avl.getNodeCount(), avl.getHeight(), avl.getRotations()));
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR);
            a.setHeaderText("Ошибка при рисовании");
            a.setContentText(e.getMessage());
            a.showAndWait();
        }
    }

    private void showAlert(String msg) { // информационное сообщ
        Alert a = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        a.showAndWait();
    }

    private boolean showConfirmAlert(String msg) { // сообщ подтверждения
        Alert a = new Alert(Alert.AlertType.CONFIRMATION, msg, ButtonType.YES, ButtonType.NO);
        return a.showAndWait().orElse(ButtonType.NO) == ButtonType.YES;
    }
}