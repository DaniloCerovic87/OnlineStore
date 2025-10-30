package com.inventoryservice.inventoryservice.parser;

import com.inventoryservice.inventoryservice.dto.ParsedRow;

import java.io.IOException;
import java.io.InputStream;

public interface RowParser {

    Iterable<ParsedRow> parse(InputStream is, String fileName) throws IOException;

}