package edu.poly.nhtr.interfaces;

import java.util.List;

import edu.poly.nhtr.models.Index;

public interface IndexInterface {
    void showDialogDetailedIndex(Index index);
    void setIndexList(List<Index> indexList);
    void showToast(String message);
    void closeDialog();
    void showLoading();
    void hideLoading();
    void showButtonLoading(int id);
    void hideButtonLoading(int id);
    void showDialogConfirmDeleteIndex(Index index);
    void closeLayoutDeleteManyRows();
    void showDialogActionSuccess(String message);
    void showLayoutNoData();
    void hideLayoutNoData();
    void getListIndexes(List<Index> indexList);
    void setWaterIndex(boolean isUsed);
    void showDialogNoteIndexStatus();
    boolean isAdded2();
    void setupLayoutForNextMonth(String homeID, int month, int year);
    String getHomeID();
}
