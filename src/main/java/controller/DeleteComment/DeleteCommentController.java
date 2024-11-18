package controller.DeleteComment;

import use_case.DeleteComment.DeleteCommentInputBoundary;
import use_case.DeleteComment.DeleteCommentInputData;

public class DeleteCommentController {

    private final DeleteCommentInputBoundary deleteCommentInputBoundary;

    public DeleteCommentController(DeleteCommentInputBoundary deleteCommentInputBoundary) {
        this.deleteCommentInputBoundary = deleteCommentInputBoundary;
    }

    /**
     * Executes the DeleteComment Use Case.
     *
     * @param commentId The ID of the comment to be deleted.
     */
    public void execute(String commentId) {
        DeleteCommentInputData inputData = new DeleteCommentInputData(commentId);

        deleteCommentInputBoundary.deleteComment(inputData);
    }

    /**
     * Executes the "switch to LoginView" Use Case.
     */
    public void switchToDeleteCommentView() {
        // dont know what this would look like yet
        deleteCommentInputBoundary.switchToDeleteCommentView();
    }
}
