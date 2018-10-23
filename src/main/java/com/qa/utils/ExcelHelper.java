package com.qa.utils;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.openxml4j.util.ZipSecureFile;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class ExcelHelper {

    private static final int FIRST_COLUMN = 0;
    private XSSFWorkbook workbook;
    private XSSFSheet activeSheet;

    public ExcelHelper loadExcelFile(String excelFilePath) {
        if (excelFilePath.length() == 0 || excelFilePath == null) {
            Reporter.error("Please enter a file path for the spreadsheet.");
            return null;
        } else {
            try {
                setWorkbook(new XSSFWorkbook(new FileInputStream(excelFilePath)));
                setActiveSheet(getWorkbook().getSheetName(0));
            } catch (FileNotFoundException e) {
                Reporter.error("Spreadsheet not found at specified path.");
            } catch (IOException e) {
                Reporter.error("Couldn't read file from the location.  This could be cause by a network issue or the library" +
                        " not having access to the file location.");
            }
            return this;
        }
    }

    public ExcelHelper loadExcelFileAsResource(String excelFilePath) {
        if (excelFilePath.length() == 0 || excelFilePath == null) {
            Reporter.error("Please enter a file path for the spreadsheet.");
            return null;
        } else {
            try {
                setWorkbook(new XSSFWorkbook(Thread.currentThread().getClass().getResourceAsStream(excelFilePath)));
                setActiveSheet(getWorkbook().getSheetName(0));
            } catch (FileNotFoundException e) {
                Reporter.error("Spreadsheet not found at specified path.");
            } catch (IOException e) {
                Reporter.error("Couldn't read file from the location.  This could be cause by a network issue or the library" +
                        " not having access to the file location.");
            }
            return this;
        }
    }

    public XSSFWorkbook getWorkbook() {
        return workbook;
    }

    private void setWorkbook(XSSFWorkbook workbook) {
        this.workbook = workbook;
    }

    public XSSFSheet getActiveSheet() {
        return activeSheet;
    }

    public ExcelHelper setActiveSheet(String worksheetName) {
        if (worksheetName == null || worksheetName.length() == 0) {
            Reporter.error("Please enter a worksheet name.");
            return null;
        } else {
            activeSheet = workbook.getSheet(worksheetName);

            if (activeSheet == null) {
                Reporter.error("Worksheet with the given name is not found in the workbook.");
                return null;
            } else {
                return this;
            }
        }
    }


    public ArrayList<Object[]> getRows(int firstRow, int lastRow) {
        XSSFSheet worksheet = getActiveSheet();
        if (firstRow <= 0) {
            firstRow = 1;
        }

        final int lastRowNum = worksheet.getLastRowNum();
        if (lastRow <= 0) {
            lastRow = 1;
        } else if (lastRow > lastRowNum) {
            lastRow = lastRowNum;
        }

        // Gets the row with the column headings
        XSSFRow columnHeadings = worksheet.getRow(0);
        // Works out the number of headings i.e. number of columns with data.
        int numberOfCells = columnHeadings.getLastCellNum() - FIRST_COLUMN;

        // Create an array containing all the column headings.
        String[] columnHeadingArray = new String[numberOfCells];
        for (int i = 0; i < numberOfCells; i++) {
            columnHeadingArray[i] = columnHeadings.getCell(i).getStringCellValue();
        }

        ArrayList<Object[]> testData = new ArrayList<Object[]>();

        // Loop through each row in the range specified.
        for (int counter = firstRow; counter <= lastRow; counter++) {
            XSSFRow row = worksheet.getRow(counter);
            HashMap<String, Object> rowHash = new HashMap<String, Object>();

            // Puts each cell into the hashmap that represents the data in a row
            for (int columnNumber = 0; columnNumber < numberOfCells; columnNumber++) {
                XSSFCell cell = row.getCell(columnNumber);

                switch (cell.getCellType()) {
                    case Cell.CELL_TYPE_STRING:
                        rowHash.put(columnHeadingArray[columnNumber], cell.getRichStringCellValue().getString());
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        if (HSSFDateUtil.isCellDateFormatted(cell)) {
                            rowHash.put(columnHeadingArray[columnNumber], cell.getDateCellValue());
                        } else {
                            rowHash.put(columnHeadingArray[columnNumber], cell.getNumericCellValue());
                        }
                        break;
                    case Cell.CELL_TYPE_BOOLEAN:
                        rowHash.put(columnHeadingArray[columnNumber], cell.getBooleanCellValue());
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        // If the cell is a formula then it will return the last calculated value as a string.
                        rowHash.put(columnHeadingArray[columnNumber], cell.getRawValue());
                        break;
                    default:
                        rowHash.put(columnHeadingArray[columnNumber], cell.getRawValue());
                        Reporter.debug("The value in cell on row " + counter + ", column " + columnNumber + " has a unknown type.");
                }
            }
            testData.add(new Object[] { rowHash });
        }
        return testData;
    }

    public ArrayList<Object[]> getAllRows() {
        final int lastRow = getActiveSheet().getLastRowNum();
        return getRows(1, lastRow);
    }

    public HashMap<String, Object> retrieveRowByName(String excelFilePath, String sheetName, String deviceName) {
        ArrayList<Object[]> rows = retrieveAllRows(excelFilePath, sheetName);
        for (Object[] a : rows) {
            HashMap<String, Object> row = (HashMap<String, Object>) a[0];
            if (row.get("name").equals(deviceName)) {
                return row;
            }
        }
        return null;
    }

    public ArrayList<Object[]> retrieveAllRows(String excelFilePath, String sheetName) {
        loadExcelFile(excelFilePath);
        if (getActiveSheet().getSheetName() != sheetName) {
            setActiveSheet(sheetName);
        }
        ArrayList<Object[]> rows = getAllRows();
        return rows;
    }

    public ArrayList<Object[]> filterMapByColumn(ArrayList<Object[]> mapToFilter, String fieldName, String[] fieldValues) {
        ArrayList<Object[]> deviceList = new ArrayList<Object[]>();

        for (String value: fieldValues) {
            deviceList.addAll(filterMapByColumn(mapToFilter, fieldName, value));
        }
        return deviceList;
    }


    public ArrayList<Object[]> filterMapByColumn(ArrayList<Object[]> mapToFilter, String fieldName, String fieldValue) {
        ArrayList<Object[]> filteredRows = new ArrayList<Object[]>();
        for (Object[] a : mapToFilter) {
            HashMap<String, Object> row = (HashMap<String, Object>) a[0];
            if (row.get(fieldName) != null && row.get(fieldName).equals(fieldValue)) {
                a[0] = row;
                filteredRows.add(a);
            }
        }
        return filteredRows;
    }
}
