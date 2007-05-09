package net.sf.jxls.controller;

import org.apache.poi.hssf.usermodel.HSSFRow;
import net.sf.jxls.util.Util;
import net.sf.jxls.util.SheetHelper;
import net.sf.jxls.util.TagBodyHelper;
import net.sf.jxls.formula.*;
import net.sf.jxls.transformation.DuplicateTransformation;
import net.sf.jxls.transformation.RemoveTransformation;
import net.sf.jxls.transformation.ShiftTransformation;
import net.sf.jxls.transformation.DuplicateTransformationByColumns;
import net.sf.jxls.controller.SheetTransformationController;
import net.sf.jxls.tag.Block;
import net.sf.jxls.tag.Point;
import net.sf.jxls.transformer.RowCollection;
import net.sf.jxls.transformer.Sheet;
import net.sf.jxls.parser.Cell;

import java.util.*;

/**
 * This class controls and saves all transforming operations on spreadsheet cells.
 * It implements {@link net.sf.jxls.controller.SheetTransformationController} interface
 * to track all cells transformations
 * @author Leonid Vysochyn
 */
public class SheetTransformationControllerImpl implements SheetTransformationController {

    List transformations = new ArrayList();

    Sheet sheet;
    TagBodyHelper helper;
    FormulaController formulaController;

    public SheetTransformationControllerImpl(Sheet sheet) {
        this.sheet = sheet;
        helper = new TagBodyHelper();
        formulaController = sheet.getWorkbook().getFormulaController();
    }

    public int duplicateDown( Block block, int n ){
        if( n > 0 ){
            if( block.getSheet() == null ){
                block.setSheet( sheet );
            }
            ShiftTransformation shiftTransformation = new ShiftTransformation(new Block(sheet, block.getEndRowNum() + 1, Integer.MAX_VALUE), n * block.getNumberOfRows(), 0);
            transformations.add( shiftTransformation);
            DuplicateTransformation duplicateTransformation = new DuplicateTransformation(block, n);
            transformations.add( duplicateTransformation );
            formulaController.updateWorkbookFormulas( shiftTransformation );
            formulaController.updateWorkbookFormulas( duplicateTransformation );
            return TagBodyHelper.duplicateDown( sheet.getHssfSheet(), block, n );
        }else{
            return 0;
        }
    }

    public int duplicateRight(Block block, int n) {
        if( n > 0 ){
            ShiftTransformation shiftTransformation = new ShiftTransformation(new Block(sheet, block.getStartRowNum(),
                    (short)(block.getEndCellNum() + 1), block.getEndRowNum(),  Short.MAX_VALUE), 0, (short) (block.getNumberOfColumns()*n));
            transformations.add( shiftTransformation );
            if( block.getSheet() == null ){
                block.setSheet( sheet );
            }
            DuplicateTransformationByColumns duplicateTransformation = new DuplicateTransformationByColumns( block, n);
            transformations.add( duplicateTransformation );
            formulaController.updateWorkbookFormulas( shiftTransformation );
            formulaController.updateWorkbookFormulas( duplicateTransformation );
            return TagBodyHelper.duplicateRight( sheet.getHssfSheet(), block, n) ;
        }else{
            return 0;
        }
    }

    public void removeBorders(Block block) {
        transformations.add( new RemoveTransformation( new Block(sheet, block.getStartRowNum(), block.getStartRowNum())));
        ShiftTransformation shiftTransformation1 = new ShiftTransformation(new Block(sheet, block.getStartRowNum() + 1, Integer.MAX_VALUE), -1, 0);
        transformations.add( shiftTransformation1 );
        transformations.add( new RemoveTransformation( new Block(sheet, block.getEndRowNum() - 1, block.getEndRowNum() - 1 ) ));
        ShiftTransformation shiftTransformation2 = new ShiftTransformation(new Block(sheet, block.getEndRowNum(), Integer.MAX_VALUE), -1, 0);
        transformations.add( shiftTransformation2 );
        formulaController.updateWorkbookFormulas( shiftTransformation1 );
        formulaController.updateWorkbookFormulas( shiftTransformation2 );
        TagBodyHelper.removeBorders( sheet.getHssfSheet(), block );

    }

    public void removeLeftRightBorders(Block block) {
        transformations.add( new RemoveTransformation( new Block( sheet, block.getStartRowNum(), block.getStartCellNum(), block.getEndRowNum(), block.getStartCellNum()) ));
        ShiftTransformation shiftTransformation1 = new ShiftTransformation( new Block(sheet, block.getStartRowNum(), (short) (block.getStartCellNum() + 1), block.getEndRowNum(), Short.MAX_VALUE ), 0, -1);
        transformations.add( shiftTransformation1 );
        transformations.add( new RemoveTransformation( new Block(sheet, block.getStartRowNum(), (short)(block.getEndCellNum() - 1), block.getEndRowNum(), (short)(block.getEndCellNum() - 1)) ));
        ShiftTransformation shiftTransformation2 = new ShiftTransformation( new Block( sheet, block.getStartRowNum(), block.getEndCellNum(), block.getEndRowNum(), Short.MAX_VALUE), 0, -1);
        transformations.add( shiftTransformation2 );
        formulaController.updateWorkbookFormulas( shiftTransformation1 );
        formulaController.updateWorkbookFormulas( shiftTransformation2 );
        TagBodyHelper.removeLeftRightBorders( sheet.getHssfSheet(), block);
//        formulaController.writeFormulas(new CommonFormulaResolver());
//        Util.writeToFile("afterRemoveLeftRightBorders.xls", sheet.getHssfWorkbook());
    }

    public void removeRowCells(HSSFRow row, short startCellNum, short endCellNum) {
        transformations.add( new RemoveTransformation( new Block(sheet, row.getRowNum(), startCellNum, row.getRowNum(), endCellNum)) );
        ShiftTransformation shiftTransformation = new ShiftTransformation(new Block(sheet, row.getRowNum(), (short) (endCellNum + 1), row.getRowNum(), Short.MAX_VALUE), 0, endCellNum - startCellNum + 1);
        transformations.add( shiftTransformation );
        transformations.add( new RemoveTransformation( new Block(sheet, row.getRowNum(), (short) (row.getLastCellNum() - (endCellNum - startCellNum)), row.getRowNum(), row.getLastCellNum())));
        formulaController.updateWorkbookFormulas( shiftTransformation );
        TagBodyHelper.removeRowCells( sheet.getHssfSheet(), row, startCellNum, endCellNum );
    }

    public void removeBodyRows(Block block) {
        if( block.getSheet() == null ){
            block.setSheet( sheet );
        }
        RemoveTransformation removeTransformation = new RemoveTransformation(block);
        transformations.add(removeTransformation);
        ShiftTransformation shiftTransformation = new ShiftTransformation(new Block(sheet, block.getEndRowNum() + 1, Integer.MAX_VALUE), -block.getNumberOfRows(), 0);
        transformations.add( shiftTransformation );
        formulaController.updateWorkbookFormulas( removeTransformation );
        formulaController.updateWorkbookFormulas( shiftTransformation );
        TagBodyHelper.removeBodyRows( sheet.getHssfSheet(), block );
    }


    public void duplicateRow(RowCollection rowCollection) {
        int startRowNum = rowCollection.getParentRow().getHssfRow().getRowNum();
        int endRowNum = startRowNum + rowCollection.getDependentRowNumber();

        Block shiftBlock = new Block(sheet, endRowNum + 1, Integer.MAX_VALUE);
        ShiftTransformation shiftTransformation = new ShiftTransformation(shiftBlock, rowCollection.getCollectionProperty().getCollection().size() - 1, 0);
        transformations.add( shiftTransformation);
        Block duplicateBlock = new Block(sheet, startRowNum, endRowNum);
        DuplicateTransformation duplicateTransformation = new DuplicateTransformation(duplicateBlock, rowCollection.getCollectionProperty().getCollection().size()-1);
        transformations.add( duplicateTransformation );
        List cells = rowCollection.getRowCollectionCells();
        for (int i = 0; i < cells.size(); i++) {
            Cell cell = (Cell) cells.get(i);
            if( cell!= null && cell.getHssfCell() != null){
                shiftBlock.addAffectedColumn( cell.getHssfCell().getCellNum() );
                duplicateBlock.addAffectedColumn( cell.getHssfCell().getCellNum() );
            }
        }
        formulaController.updateWorkbookFormulas( shiftTransformation );
        formulaController.updateWorkbookFormulas( duplicateTransformation );
        Util.duplicateRow( rowCollection );
    }

    public List getTransformations() {
        return transformations;
    }

    public Sheet getSheet() {
        return sheet;
    }
}
