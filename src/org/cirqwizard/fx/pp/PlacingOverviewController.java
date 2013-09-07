/*
This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License version 3 as published by
    the Free Software Foundation.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.
*/

package org.cirqwizard.fx.pp;

import org.cirqwizard.fx.SceneController;
import org.cirqwizard.pp.ComponentId;
import org.cirqwizard.toolpath.PPPoint;
import org.cirqwizard.toolpath.Toolpath;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;


public class PlacingOverviewController extends SceneController implements Initializable
{
    @FXML private Parent view;

    @FXML private TableView<Row> table;
    @FXML private TableColumn<Row, String> packageColumn;
    @FXML private TableColumn<Row, String> valueColumn;
    @FXML private TableColumn<Row, Integer> quantityColumn;
    @FXML private TableColumn<Row, String> namesColumn;

    private ObservableList<Row> rows;

    @Override
    public Parent getView()
    {
        return view;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        packageColumn.setCellValueFactory(new PropertyValueFactory<Row, String>("packaging"));
        valueColumn.setCellValueFactory(new PropertyValueFactory<Row, String>("value"));
        quantityColumn.setCellValueFactory(new PropertyValueFactory<Row, Integer>("quantity"));
        namesColumn.setCellValueFactory(new PropertyValueFactory<Row, String>("names"));

        rows = FXCollections.observableArrayList();
        table.setItems(rows);
    }

    @Override
    public void refresh()
    {
        HashMap<ComponentId, Row> rowsMap = new HashMap<ComponentId, Row>();
        for (Toolpath t : getMainApplication().getContext().getComponentsLayer().getToolpaths())
        {
            PPPoint p = (PPPoint) t;
            if (rowsMap.get(p.getId()) == null)
            {
                rowsMap.put(p.getId(), new Row(p.getId().getPackaging(), p.getId().getValue(), 1, p.getName()));
            }
            else
            {
                Row row = rowsMap.get(p.getId());
                row.setQuantity(row.getQuantity() + 1);
                row.setNames(row.getNames() + ", " + p.getName());
            }
        }
        rows.clear();
        rows.addAll(rowsMap.values());

    }

    public static class Row
    {
        private StringProperty packaging;
        private StringProperty value;
        private IntegerProperty quantity;
        private StringProperty names;

        public Row(String packaging, String value, Integer quantity, String names)
        {
            this.packaging = new SimpleStringProperty(packaging);
            this.value = new SimpleStringProperty(value);
            this.quantity = new SimpleIntegerProperty(quantity);
            this.names = new SimpleStringProperty(names);
        }

        public String getPackaging()
        {
            return packaging.get();
        }

        public StringProperty packagingProperty()
        {
            return packaging;
        }

        public String getValue()
        {
            return value.get();
        }

        public StringProperty valueProperty()
        {
            return value;
        }

        public Integer getQuantity()
        {
            return quantity.get();
        }

        public IntegerProperty quantityProperty()
        {
            return quantity;
        }

        public String getNames()
        {
            return names.get();
        }

        public StringProperty namesProperty()
        {
            return names;
        }

        public void setQuantity(Integer quantity)
        {
            this.quantity.set(quantity);
        }

        public void setNames(String names)
        {
            this.names.set(names);
        }
    }

}
