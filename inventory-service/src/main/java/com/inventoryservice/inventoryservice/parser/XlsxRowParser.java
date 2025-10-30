package com.inventoryservice.inventoryservice.parser;

import com.inventoryservice.inventoryservice.dto.ParsedRow;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.springframework.stereotype.Component;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class XlsxRowParser implements RowParser {

    @Override
    public Iterable<ParsedRow> parse(InputStream is, String fileName) throws IOException {
        List<ParsedRow> rows = new ArrayList<>();
        try (Workbook workbook = WorkbookFactory.create(is)) {
            Sheet sheet = workbook.getSheetAt(0);
            int lineNo = 0;

            for (Row row : sheet) {
                lineNo++;

                if (lineNo == 1) {
                    continue;
                }

                try {
                    String sku = row.getCell(0).getStringCellValue().trim();
                    int qty = (int) row.getCell(1).getNumericCellValue();
                    rows.add(new ParsedRow(lineNo, sku, qty));
                } catch (Exception e) {
                    log.info("Skipping malformed row {} in file {}: {}", lineNo, fileName, e.getMessage());
                }
            }
        }
        return rows;
    }
}
