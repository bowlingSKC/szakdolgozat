package balint.lenart.controllers.main;

import balint.lenart.dao.postgres.PostgresConnection;
import balint.lenart.utils.DbUtil;
import balint.lenart.utils.NotificationUtil;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.util.Callback;
import org.apache.commons.lang3.StringUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;

public class PostgresQueryController {

    @FXML private TextArea queryArea;
    @FXML private TableView resultTable;

    @FXML
    private void handleQuery() {
        resultTable.getColumns().clear();
        resultTable.getItems().clear();

        try {
            ResultSet resultSet = getResultByQuery();
            showResult( resultSet );
        } catch (SQLException ex) {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Hiba", "Szintaktikai hiba!",
                    ex.getMessage());
        } catch (Exception ex) {
            NotificationUtil.showNotification(Alert.AlertType.ERROR, "Hiba", "Hiba történt a parancs futtatása közben!",
                    ex.getMessage());
        }
    }

    private ResultSet getResultByQuery() throws SQLException {
        Statement statement = PostgresConnection.getInstance().getConnection().createStatement();
        return statement.executeQuery(queryArea.getText().trim());
    }

    private void showResult(ResultSet resultSet) throws SQLException {
        createColumns( resultSet );
        fillTableFromResult( resultSet );
    }

    private void createColumns(ResultSet resultSet) throws SQLException {
        ResultSetMetaData metaData = resultSet.getMetaData();
        for(int i = 0; i < metaData.getColumnCount(); i++) {
            final int j = i;
            TableColumn col = new TableColumn(resultSet.getMetaData().getColumnName(i+1) + "\n["+ DbUtil.getJdbcTypeName(metaData.getColumnType(i+1)) +"]");
            col.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList,String>,ObservableValue<String>>(){
                public ObservableValue<String> call(TableColumn.CellDataFeatures<ObservableList, String> param) {
                    return new SimpleStringProperty((String) param.getValue().get(j));
                }
            });

            col.setCellFactory(param -> new CenterTableCellWithTooltip());

            resultTable.getColumns().addAll(col);
        }
    }

    private void fillTableFromResult(ResultSet resultSet) throws SQLException {
        ObservableList<ObservableList> data = FXCollections.observableArrayList();

        while(resultSet.next()){
            ObservableList<String> row = FXCollections.observableArrayList();
            for(int i=1 ; i<=resultSet.getMetaData().getColumnCount(); i++){
                row.add(resultSet.getString(i));
            }
            data.add(row);
        }

        resultTable.setItems(data);
    }

    private class CenterTableCellWithTooltip extends TableCell {

        public CenterTableCellWithTooltip() {
            super();
            setAlignment(Pos.BASELINE_LEFT);
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            setText(empty ? null : getString());
            setGraphic(null);

            if(StringUtils.isNotEmpty(getString())) {
                setTooltip(new Tooltip(getString()));
            }
        }

        private String getString() {
            return getItem() == null ? "" : getItem().toString();
        }
    }
}
