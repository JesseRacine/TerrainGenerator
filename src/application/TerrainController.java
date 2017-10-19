package application;

import fractal.DisplaceFractal;
import fractal.PerlinFractal;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;


public class TerrainController {

    @FXML
    private Slider sBlends;

    @FXML
    private Slider sharpen;

    @FXML
    private Slider freq;

    @FXML
    private CheckBox pre;

    @FXML
    private CheckBox post;

    @FXML
    private ChoiceBox<?> interpBox;

    @FXML
    private Button go;

    @FXML
    private TextField sizeField;

    @FXML
    private Slider dispSharpen;

    @FXML
    private Slider roughSlide;

    @FXML
    private Slider mountSize;

    @FXML
    private CheckBox dispPost;

    @FXML
    private RadioButton sizeOne;

    @FXML
    private ToggleGroup sizegroup;

    @FXML
    private RadioButton sizeTwo;

    @FXML
    private RadioButton sizeThree;

    @FXML
    private RadioButton sizeFour;

    @FXML
    private Button dispGo;

    @FXML
    private Button show;

    @FXML
    private Button remove;

    @FXML
    private RadioButton bmpOutput;

    @FXML
    private ToggleGroup outputgroup;

    @FXML
    private RadioButton rawOutput;

    @FXML
    private CheckBox overwrite;

    @FXML
    private Canvas canvas1;

    @FXML
    private Canvas canvas2;

    @FXML
    private Canvas canvas3;

    private Canvas canvases[];

    @FXML
    private Rectangle highlightBorder1;

    @FXML
    private Rectangle highlightBorder3;

    @FXML
    private Rectangle highlightBorder2;

    private WritableImage wimarray[];

    private int selectedCanvas;

    private Stage stage;


    public TerrainController() {
    }

    @FXML
    protected void initialize() {
        ObservableList obs = FXCollections.observableArrayList("Linear", "Cosine", "Cubic", "Standard");
        interpBox.setItems(obs);
        interpBox.getSelectionModel().selectFirst();


        GraphicsContext gc1 = canvas1.getGraphicsContext2D();
        GraphicsContext gc2 = canvas2.getGraphicsContext2D();
        GraphicsContext gc3 = canvas3.getGraphicsContext2D();

        gc1.setFill(Color.BLACK);
        gc2.setFill(Color.BLACK);
        gc3.setFill(Color.BLACK);

        gc1.fillRect(0, 0, 128, 128);
        gc2.fillRect(0, 0, 128, 128);
        gc3.fillRect(0, 0, 128, 128);

        selectedCanvas = -1;

        sizeField.textProperty().addListener((a, b, c) -> textChanged(a, b, c));

        sharpen.setLabelFormatter(new SliderLabeler());
        dispSharpen.setLabelFormatter(new SliderLabeler());

        wimarray = new WritableImage[3];

        canvases = new Canvas[3];

        canvases[0] = canvas1;
        canvases[1] = canvas2;
        canvases[2] = canvas3;
    }

    public void setStage(Stage stage) {
        this.stage = stage;

    }

    @FXML
    void canvas1Clicked(MouseEvent event) {
        selectedCanvas = 0;
        highlightBorder1.setVisible(true);
        highlightBorder2.setVisible(false);
        highlightBorder3.setVisible(false);

    }

    @FXML
    void canvas2Clicked(MouseEvent event) {
        selectedCanvas = 1;
        highlightBorder1.setVisible(false);
        highlightBorder2.setVisible(true);
        highlightBorder3.setVisible(false);
    }

    @FXML
    void canvas3Clicked(MouseEvent event) {
        selectedCanvas = 2;
        highlightBorder1.setVisible(false);
        highlightBorder2.setVisible(false);
        highlightBorder3.setVisible(true);

    }

    @FXML
    void displacementRender(ActionEvent event) {
        DisplaceFractal.DisplaceSettings dispSettings = new DisplaceFractal.DisplaceSettings();
        dispSettings.contrast = (int) dispSharpen.getValue();
        dispSettings.roughness = (int) roughSlide.getValue();
        dispSettings.mountainSize = (int) mountSize.getValue();
        dispSettings.postSmooth = dispPost.isSelected();

        int size = 0;
        if (sizeOne.isSelected())
            size = 129;
        else if (sizeTwo.isSelected())
            size = 257;
        else if (sizeThree.isSelected())
            size = 513;
        else if (sizeFour.isSelected())
            size = 1025;

        DisplaceFractal df = new DisplaceFractal(size);
        WritableImage im = df.render(dispSettings);
        addImage(im);

    }

    void textChanged(ObservableValue o, String oldVal, String newVal) {
        // block textfield so that only integer entries (or blank) is allowed
        if (newVal.length() != 0) {
            try {
                int v = Integer.parseInt(newVal);
            } catch (Exception e) {
                sizeField.setText(oldVal);
            }
        }

    }

    @FXML
    void perlRender(ActionEvent event) {
        int size = 200;
        try {
            int v = Integer.parseInt(sizeField.getText());
            size = v;
        } catch (Exception e) {
        }
        PerlinFractal.PerlinSettings pset = new PerlinFractal.PerlinSettings();
        pset.blends = (int) sBlends.getValue();
        pset.preSmooth = pre.isSelected();
        pset.postSmooth = post.isSelected();
        pset.maxBright = (int) sharpen.getValue();
        pset.freqReduc = (int) freq.getValue();
        pset.interp = interpBox.getSelectionModel().getSelectedIndex();

        PerlinFractal pf = new PerlinFractal(size);


        WritableImage im = pf.render(pset);

        addImage(im);
    }

    private void addImage(WritableImage theImage) {
        for (int i = 0; i < 3; i++) {
            if (wimarray[i] == null || overwrite.isSelected()) {
                wimarray[i] = theImage;
                GraphicsContext gc = canvases[i].getGraphicsContext2D();
                gc.drawImage(theImage, 0, 0);
                return;
            }
        }
    }

    @FXML
    void removeImage() {
        if (selectedCanvas == -1) return;

        wimarray[selectedCanvas] = null;
        GraphicsContext gc = canvases[selectedCanvas].getGraphicsContext2D();
        gc.setFill(Color.BLACK);
        gc.fillRect(0, 0, 128, 128);
        highlightBorder1.setVisible(false);
        highlightBorder2.setVisible(false);
        highlightBorder3.setVisible(false);

        selectedCanvas = -1;

    }

    @FXML
    void showImage() {
        if (selectedCanvas == -1) return;
        Stage stage = new Stage();
        stage.setTitle("Fractal Image");
        int size = (int) wimarray[selectedCanvas].getWidth();

        Canvas cv = new Canvas(size, size);
        cv.setVisible(true);
        GraphicsContext g = cv.getGraphicsContext2D();
        g.drawImage(wimarray[selectedCanvas], 0, 0);

        Pane pane = new Pane();
        pane.getChildren().add(cv);

        Scene scene = new Scene(pane);
        stage.setScene(scene);
        stage.show();
    }

    @FXML
    void saveImage(ActionEvent event) {
        if (selectedCanvas == -1) return;

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Save Location");
        if (bmpOutput.isSelected())
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Bitmap Image", "*.bmp"));
        else
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Raw Binary", "*.raw"));

        File f = fileChooser.showSaveDialog(stage);
        System.out.println("Got " + f);

        if (f != null) {
            if (bmpOutput.isSelected()) {

                /* Note that all of the below nonsense has to do with saving bitmaps. By default, the
                SwingFXUtils.fromFXImage makes a buffered image with transparency channel. We have to make a separate
                bufferedimage and explicitly remove the transparency channel, and then copy the bufferedimage
                into this one. If you don't do that, the ImageIO.write method fails to write the bitmap to the
                hard drive.
                 */
                try {
                    Image im = wimarray[selectedCanvas];
                    BufferedImage bi = new BufferedImage((int) im.getWidth(), (int) im.getHeight(), BufferedImage.TYPE_INT_RGB);
                    BufferedImage bi2 = SwingFXUtils.fromFXImage(im, null);
                    bi.getGraphics().drawImage(bi2, 0, 0, null);
                    ImageIO.write(bi, "bmp", f);

                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to save file").showAndWait();
                }
            } else {
                try {
                    FileOutputStream out = new FileOutputStream(f);
                    Image im = wimarray[selectedCanvas];
                    PixelReader pr = im.getPixelReader();
                    int w = (int) im.getWidth();
                    for (int i = 0; i < w; i++) {
                        for (int j = 0; j < w; j++) {
                            int c = (int) (pr.getColor(j, i).getRed() * 255);
                            out.write(c);
                        }
                    }
                    out.close();

                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Failed to save file").showAndWait();
                }


            }


        }

    }

}
