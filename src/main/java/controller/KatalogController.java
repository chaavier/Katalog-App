package controller;

import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Optional;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import data.ProdukRepository;
import model.Produk;

public class KatalogController {

    @FXML
    private TextField namaField;
    @FXML
    private TextField hargaField;
    @FXML
    private ComboBox<String> fotoBox;
    @FXML
    private Label pesanLabel;
    @FXML
    private Label jumlahLabel;
    @FXML
    private TilePane katalogPane;
    private final ProdukRepository repository = new ProdukRepository();

    @FXML
    private void initialize() {
        fotoBox.getItems().add("roti.png");
        fotoBox.getItems().add("susu.png");
        fotoBox.getItems().add("kopi.png");
        fotoBox.setValue("roti.png");
        try {
            showData();
        } catch (SQLException e) {
            pesanLabel.setText("Gagal membaca database. Lihat Output.");
            e.printStackTrace();
        }
    }

    private void showData() throws SQLException {
        ArrayList<Produk> daftar = repository.getAll();
        katalogPane.getChildren().clear();
        for (Produk produk : daftar) {
            VBox kartu = createProductCard(produk);
            katalogPane.getChildren().add(kartu);
        }
        jumlahLabel.setText("Jumlah produk: " + daftar.size());
    }

    private VBox createProductCard(Produk produk) {
        ImageView fotoView = new ImageView();
        fotoView.setFitWidth(170);
        fotoView.setFitHeight(110);
        fotoView.setPreserveRatio(true);
        StackPane bingkai = new StackPane();
        bingkai.setPrefSize(180, 120);
        bingkai.setMinSize(180, 120);
        bingkai.setMaxSize(180, 120);

        URL lokasi = getClass().getResource("/image/" + produk.getFoto());
        if (lokasi != null) {
            Image gambar = new Image(lokasi.toExternalForm(),
                    170, 110, true, true);
            if (!gambar.isError()) {
                fotoView.setImage(gambar);
                bingkai.getChildren().add(fotoView);
            } else {
                bingkai.getChildren().add(new Label("Gambar tidak terbaca"));
            }
        } else {
            bingkai.getChildren().add(new Label("Gambar tidak tersedia"));
        }

        Label namaLabel = new Label(produk.getNama());
        namaLabel.setWrapText(true);
        namaLabel.getStyleClass().add("nama-produk");
        Label hargaLabel = new Label("Rp" + produk.getHarga());
        Label idLabel = new Label("ID: " + produk.getId());
        idLabel.getStyleClass().add("muted");

        VBox kartu = new VBox();
        kartu.setSpacing(8);
        kartu.setMinWidth(220);
        kartu.setPrefWidth(220);
        kartu.setMaxWidth(220);
        kartu.getStyleClass().add("kartu-produk");
        kartu.getChildren().add(bingkai);
        kartu.getChildren().add(namaLabel);
        kartu.getChildren().add(hargaLabel);
        kartu.getChildren().add(idLabel);

        Button editButton = new Button("Edit");
        editButton.getStyleClass().add("button-edit");
        editButton.setOnAction(e -> enableInlineEdit(kartu, produk));
        Button hapusButton = new Button("Hapus");
        hapusButton.getStyleClass().add("button-hapus");
        hapusButton.setOnAction(e -> deleteProduct(produk));
        HBox aksiBox = new HBox(8, editButton, hapusButton);
        kartu.getChildren().add(aksiBox);
        return kartu;
    }

    // Mengubah isi kartu menjadi form edit (nama, harga, foto).
    private void enableInlineEdit(VBox kartu, Produk produk) {
        TextField editNamaField = new TextField(produk.getNama());
        editNamaField.setPromptText("Nama produk");

        TextField editHargaField = new TextField(String.valueOf(produk.getHarga()));
        editHargaField.setPromptText("Harga (Rp)");

        ComboBox<String> editFotoBox = new ComboBox<>();
        editFotoBox.getItems().addAll(fotoBox.getItems());
        editFotoBox.setValue(produk.getFoto());
        editFotoBox.setMaxWidth(Double.MAX_VALUE);

        Label infoIdLabel = new Label("Edit ID: " + produk.getId());
        infoIdLabel.getStyleClass().add("muted");

        Label cardPesanLabel = new Label();
        cardPesanLabel.setWrapText(true);

        Button simpanEditButton = new Button("Simpan");
        simpanEditButton.getStyleClass().add("button-primary");
        simpanEditButton.setOnAction(e -> saveInlineEdit(
                produk, editNamaField, editHargaField, editFotoBox, cardPesanLabel));

        Button batalEditButton = new Button("Batal");
        batalEditButton.setOnAction(e -> {
            try {
                showData();
            } catch (SQLException ex) {
                pesanLabel.setText("Gagal memuat ulang katalog. Lihat Output.");
                ex.printStackTrace();
            }
        });

        HBox aksiEditBox = new HBox(8, simpanEditButton, batalEditButton);
        kartu.getChildren().setAll(
                infoIdLabel,
                editNamaField,
                editHargaField,
                editFotoBox,
                aksiEditBox,
                cardPesanLabel
        );
    }

    private void saveInlineEdit(
            Produk produkLama,
            TextField namaInput,
            TextField hargaInput,
            ComboBox<String> fotoInput,
            Label cardPesanLabel) {
        Produk produkBaru;
        try {
            String nama = namaInput.getText();
            long harga = Long.parseLong(hargaInput.getText().trim());
            String foto = fotoInput.getValue();
            produkBaru = new Produk(produkLama.getId(), nama, harga, foto);
        } catch (NumberFormatException e) {
            cardPesanLabel.setText("Harga harus angka bulat, contoh 12000.");
            return;
        } catch (IllegalArgumentException e) {
            cardPesanLabel.setText(e.getMessage());
            return;
        }

        boolean berhasil;
        try {
            berhasil = repository.update(produkBaru);
        } catch (SQLException e) {
            cardPesanLabel.setText("Gagal mengubah data. Lihat Output.");
            e.printStackTrace();
            return;
        }

        try {
            showData();
            if (berhasil) {
                pesanLabel.setText("Produk ID " + produkLama.getId() + " berhasil diperbarui.");
            } else {
                pesanLabel.setText("Produk ID " + produkLama.getId() + " tidak ditemukan.");
            }
        } catch (SQLException e) {
            pesanLabel.setText("Data diperbarui, tetapi katalog gagal dimuat ulang.");
            e.printStackTrace();
        }
    }

    private void deleteProduct(Produk produk) {
        Alert konfirmasi = new Alert(Alert.AlertType.CONFIRMATION);
        konfirmasi.setTitle("Konfirmasi Hapus");
        konfirmasi.setHeaderText("Hapus produk \"" + produk.getNama() + "\"?");
        konfirmasi.setContentText("Data yang dihapus tidak dapat dikembalikan.");

        Optional<ButtonType> pilihan = konfirmasi.showAndWait();
        if (pilihan.isEmpty() || pilihan.get() != ButtonType.OK) {
            return;
        }

        boolean terhapus;
        try {
            terhapus = repository.delete(produk.getId());
        } catch (SQLException e) {
            pesanLabel.setText("Gagal menghapus produk. Lihat Output.");
            e.printStackTrace();
            return;
        }

        try {
            showData();
            if (terhapus) {
                pesanLabel.setText("Produk \"" + produk.getNama() + "\" berhasil dihapus.");
            } else {
                pesanLabel.setText("Produk ID " + produk.getId() + " tidak ditemukan.");
            }
        } catch (SQLException e) {
            pesanLabel.setText("Data dihapus, tetapi katalog gagal dimuat ulang.");
            e.printStackTrace();
        }
    }

    @FXML
    private void saveProduct() {
        Produk produk;
        try {
            String nama = namaField.getText();
            long harga = Long.parseLong(hargaField.getText().trim());
            String foto = fotoBox.getValue();
            produk = new Produk(nama, harga, foto);
        } catch (NumberFormatException e) {
            pesanLabel.setText("Harga harus angka bulat, contoh 12000.");
            return;
        } catch (IllegalArgumentException e) {
            pesanLabel.setText(e.getMessage());
            return;
        }
        try {
            repository.add(produk);
        } catch (SQLException e) {
            pesanLabel.setText("Gagal menyimpan. Lihat Output.");
            e.printStackTrace();
            return;
        }
        namaField.clear();
        hargaField.clear();
        try {
            showData();
            pesanLabel.setText("Produk berhasil disimpan.");
        } catch (SQLException e) {
            pesanLabel.setText("Data tersimpan, tetapi katalog gagal dimuat ulang.");
            e.printStackTrace();
        }
    }
}
