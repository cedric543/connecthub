package controller.DeleteComment;


public class DeleteCommentViewModel {
    private String commentId;
    private String errorMessage;


    // Getters and setters
    public void setEntryID(String entryID) {
        this.commentId = entryID;
    }

    public String getEntryId() {
        return commentId;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}


