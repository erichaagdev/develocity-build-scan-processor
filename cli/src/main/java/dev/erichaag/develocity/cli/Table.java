package dev.erichaag.develocity.cli;

import com.jakewharton.picnic.CellStyle;
import com.jakewharton.picnic.TableSection;

import java.util.ArrayList;
import java.util.List;

class Table {

    private final String[] header;
    private final List<String[]> rows = new ArrayList<>();

    private Table(String[] header) {
        this.header = header;
    }

    static Table withHeader(String... header) {
        return new Table(header);
    }

    Table row(String... values) {
        this.rows.add(values);
        return this;
    }

    @Override
    public String toString() {
        return new com.jakewharton.picnic.Table.Builder()
                .setCellStyle(buildCellStyle())
                .setHeader(buildHeader())
                .setBody(buildRows())
                .build()
                .toString();
    }

    private CellStyle buildCellStyle() {
        return new CellStyle.Builder()
                .setBorder(true)
                .build();
    }

    private TableSection buildHeader() {
        return new TableSection.Builder()
                .addRow(header)
                .build();
    }

    private TableSection buildRows() {
        final var bodyBuilder = new TableSection.Builder();
        rows.forEach(bodyBuilder::addRow);
        return bodyBuilder.build();
    }
}
