package cs1302.gallery;

import javafx.scene.control.ProgressBar;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.MenuBar;
import javafx.scene.control.Button;
import javafx.scene.control.Menu;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.application.Platform;
import javafx.scene.control.ToolBar;
import javafx.scene.layout.TilePane;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.control.TextField;
import javafx.scene.control.TextArea;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import java.net.URLEncoder;
import java.io.UnsupportedEncodingException;
import com.google.gson.JsonParser;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.MalformedURLException;
import java.io.IOException;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import com.google.gson.*;
import java.util.Arrays;
import com.google.gson.reflect.TypeToken;
import com.google.gson.reflect.*;
import java.util.*;
import javafx.scene.text.Text;
import java.lang.Math;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Dialog;
import javafx.animation.Timeline;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Animation;
import javafx.stage.Modality;


/**
 * Represents an iTunes GalleryApp!.
 */
public class GalleryApp extends Application {

    MenuItem exit = new MenuItem("Exit");
    final Menu file = new Menu("File");
    final Menu help = new Menu("Help");
    MenuItem about = new MenuItem("About Awrish Khan");
//    TextField search = new TextField("rock");
//    TextField search = new TextField("grunge");
    TextField search = new TextField("indie");
    Label squ = new Label("Search Query:");
    Button loader = new Button("Update Images");
    Button play = new Button("Play");
    private List<String> logos = new ArrayList<String>();
    private List<String> alternate = new ArrayList<String>();
    private List<String> showing = new ArrayList<String>();

    int numResults;

    ImageView [] pics = new ImageView[20];
    ImageView [] swap;

    TilePane tile = new TilePane();

    ImageView imgView;

    int count = 0;

    private static final int DEF_HEIGHT = 100;
    private static final int DEF_WIDTH = 100; //were both 500

    ProgressBar progressBar = new ProgressBar(0);

    boolean opening = true;

    Timeline timeline = new Timeline();


    /** {@inheritdoc}
     * The {@code start} method of the program. Creates and initializes variables
     * and sets the stage and scene.
     */
    @Override
    public void start(Stage stage) {
        VBox start = new VBox();
        EventHandler<ActionEvent> handler = event -> random();
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
//        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(keyFrame);
        timeline.pause();

        EventHandler<ActionEvent> playPause = event -> {
            if (play.getText().equals("Play")) {
                play.setText("Pause");
                timeline.play();
            } else {
                play.setText("Play");
                timeline.pause();
            }
        };

        play.setOnAction(playPause);

        opening = false;
        HBox pane = new HBox();
        Scene scene = new Scene(start);
        MenuBar menuBar = new MenuBar();
        ToolBar toolBar = new ToolBar();
        ToolBar progress = new ToolBar();

        toolBar.getItems().addAll(play, squ, search, loader);

        exit.setOnAction(new EventHandler<ActionEvent>()  {
                @Override public void handle(ActionEvent e) {
                    Platform.exit();
                }
            });

        loader.setOnAction(event -> loadImage());

        about.setOnAction(event -> help());

        tile.setPrefRows(4);
        tile.setPrefColumns(5);
        file.getItems().add(exit);
        help.getItems().add(about);
        menuBar.getMenus().addAll(file, help);
        pane.getChildren().addAll(menuBar);
        pane.setHgrow(menuBar, Priority.ALWAYS);
        start.getChildren().addAll(pane,toolBar, tile, progress);
        start.setPadding(new Insets(0,0,400,0));
        progress.getItems().addAll(progressBar, new Text("Images provided courtesy of iTunes"));
        tile.setPrefWidth(500);
        loadImage();
        stage.setMaxWidth(640);
        stage.setMaxHeight(650);
        stage.setTitle("GalleryApp!");
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    } // start


    /**
     * Creates a second window which displays information about me.
     *
     */
    public void help() {
        Stage help = new Stage();
        VBox outer = new VBox();
        ImageView pic = new ImageView(
            "file:resources/pic2.png"
            );
        help.initModality(Modality.APPLICATION_MODAL);
        Text name = new Text("Awrish Khan");
        Text email = new Text("Contact at : aak98615@uga.edu");
        Text ver = new Text("Version 1.0");
        Scene scene = new Scene(outer);
        outer.getChildren().addAll(pic,name,email,ver);
        help.setScene(scene);
        help.sizeToScene();
        help.show();
    } // help

    /**
     * Replaces images in the {@code TilePane} with artwork
     * that is not currently being shown in the {@code TilePane}.
     */
    private void random() {
        final int replace = (int)(Math.random() * 20);
//        System.out.println(replace);
        logos.add(showing.get(replace));  //swaps the image from showing to logos
        showing.remove(replace);
        final int logo = (int)(Math.random() * logos.size());  //added
        final String repUrl = logos.get(logo);
        showing.add(logos.get(logo));
        final Image repImage = new Image(repUrl);
        logos.remove(logo);
        Platform.runLater(() -> pics[replace].setImage(repImage));
    } //random

    /**
     * Method used from the Thread reading.
     *
     * @param e the {@code Runnable} whose code code is executed when
     * this thread is started.
     */
    public static void runNow(Runnable e) {
        Thread t = new Thread(e);
        t.setDaemon(true);
        t.start();
    } //runNow


    /**
     * Loads the Images using the list of Urls intialized by the
     * {@code parser} method. Contains the method call which increments
     * the progressBar.
     */
    private void loadImage() {
        Runnable task1 = () -> {
            parser();          //used to initialize the logos list
            if (logos.size() > 20) {
                try {
                    beforeComp();
                    for (int i = 0; i < 20; i++) {
//                        System.out.println("LOAD LOGOSIZE: " + logos.size());
                        Image newImage = new Image(logos.get(i), DEF_HEIGHT, DEF_WIDTH,false,false);
//                        System.out.println("lo: " + logos.get(i));
                        if (newImage.isError()) {
                            throw new IOException(newImage.getException() + "Excption");
                        } else {
                            pics[i] = new ImageView(newImage);
//                            System.out.println("set" + i);   //used for debugging
//                            System.out.println(logos.size());      //used for debugging
                        }
                        setProgress(1.0 * i / 20);
                    } //for
                    swap();
                    Runnable task = () -> getTilePane();
                    Platform.runLater(task);
                } catch (IOException | IllegalArgumentException e) {
                    Runnable alertTask = () -> alert(e);
                    Platform.runLater(alertTask);
                } catch (NullPointerException npe) {
                    Runnable alertTask = () -> alert(npe);
                    Platform.runLater(alertTask);
                } finally {
                    uponCompletion();
                }
            } else {
                Runnable listTask = () -> listAlert();
                Platform.runLater(listTask);

            } //else
        };

        runNow(task1);
//        Platform.runLater(task1);
    }


    /**
     * Helper method used to take the displayed Images out
     * of the Logos list to better help the {@code random} method
     * with swapping unused images into the {@code TilePane}.
     */
    public void swap() {
        for (int i = 0; i < 20; i++) {
//            System.out.println("SHOWSIZE " + showing.size() + "\nLOGOSSIZE: " + logos.size());
//            System.out.println("-----------------------------------");
            showing.add(logos.get(i));
//            System.out.println("showadd " + i);
        } //for
        final int stop = logos.size() - 20;
        for (int i = 20; i > 0; i--) {
            logos.remove(i);
//            System.out.println("LOGOREMOVE " + i);
        }
    } //swap

    /**
     * Helper method used insdie a Lambda expression in loadImage.
     * Modifies the {@code TilePane} child in the scene.
     */
    public void getTilePane() {
        tile.getChildren().clear();
        for (int i = 0; i < 20; i++) {
            tile.getChildren().addAll(pics[i]);
        }
    } //getTilePane

    /**
     * Updates the {@code ProgressBar}.
     * @param progress The
     *
     */
    public void setProgress(final double progress) {
        Platform.runLater(() -> progressBar.setProgress(progress));
    } //setProgress


    /**
     * Using {@code JSON}, parses through the output from
     * searching the API with the Url and intializes the
     * list of Urls.
     *
     */
    private void parser() {
        try {
//            logos.clear();
//            showing.clear();
            alternate.clear();
            String testing = "";
            testing += search.getText();
            testing = URLEncoder.encode(testing, "UTF-8");
            String base = "https://itunes.apple.com/search?term=";
            String end = "&limit=100";
            testing = base + testing + end;
//            testing = "https://deelay.me/5000/" + testing; //for the dealy to test progress bar
            URL url = new URL(testing);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            JsonElement je = JsonParser.parseReader(reader);
//            System.out.println(je);  //works
            JsonObject root = je.getAsJsonObject();
            JsonArray results = root.getAsJsonArray("results");
            numResults = results.size();
//            System.out.println("numResults: " + numResults);
            for (int i = 0; i < numResults; i++) {
                if (results.get(i).equals(null)) {
                    results.remove(i);
                } else {
                    JsonObject result = results.get(i).getAsJsonObject();
                    JsonElement artworkUrl100 = result.get("artworkUrl100");
//                    if (!logos.contains(artworkUrl100.getAsString())) {
//                        logos.add(artworkUrl100.getAsString());
//                    }
//                    System.out.println("LOGOS SIZE: " + logos.size());
                    if (!alternate.contains(artworkUrl100.getAsString())) {
                        alternate.add(artworkUrl100.getAsString());
                    }
                    if (alternate.size() > 20) {
                        logos.clear();
                        showing.clear();
                        for (int count = 0; count < alternate.size(); count++) {
                            logos.add(alternate.get(count));
                        }
                    }
                }
            } // outer for
//            System.out.println("Logos size: " + logos.size());
//            System.out.println("Alternate size: " + alternate.size());
        } catch (IOException | IllegalArgumentException e) {
            Runnable alertTask = () -> alert(e);
            Platform.runLater(alertTask);
        } // try-catch-finally
    } // parser




    /**
     * Method that helps the app function better. Also
     * sets up the progress bar and allows it to update.
     *
     */
    private void beforeComp() {
        loader.setDisable(true);
        progressBar.setProgress(0);
        timeline.pause();
    }

    /**
     * Resets button values and further
     * configures app after images are downloaded.
     */
    private void uponCompletion() {
        loader.setDisable(false);
        setProgress(1);
        timeline.play();
    }

    /**
     * Creates a pop-up window which displays the {@code alert}
     * without exiting or crashing the program. The user can dismiss
     * the window and return back to the program.
     * @param e The {@code exception} which triggers the alert.
     */
    private void alert(Exception e) {
        Alert alertDialog = new Alert(AlertType.ERROR);
        TextArea alertText = new TextArea(e.toString());
        alertDialog.getDialogPane().setContent(alertText);
        alertDialog.setResizable(true);
        alertDialog.showAndWait();
    } //alert

    /**
     * Notifies users when list size of loaded images is not of
     * sufficient size.
     *
     */
    private void listAlert() {
        Alert alertDialog = new Alert(AlertType.WARNING);
        TextArea alertText = new TextArea("List size is <= 20");
        alertDialog.getDialogPane().setContent(alertText);
        alertDialog.setResizable(true);
        alertDialog.showAndWait();
    } //listAlert

} // GalleryApp
