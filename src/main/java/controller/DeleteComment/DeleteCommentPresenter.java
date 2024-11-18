package controller.DeleteComment;

import use_case.DeleteComment.DeleteCommentOutputBoundary;
import use_case.DeleteComment.DeleteCommentOutputData;

/**
 * The Presenter for the DeleteComment Use Case.
 */
public class DeleteCommentPresenter implements DeleteCommentOutputBoundary {
    private final DeleteCommentViewModel DeleteCommentViewModel;

    public DeleteCommentPresenter(DeleteCommentViewModel viewModel) {
        this.DeleteCommentViewModel = viewModel;
    }

    @Override
    public void prepareSuccessView(DeleteCommentOutputData outputData) {

    }

    @Override
    public void prepareFailView(String errorMessage) {
        DeleteCommentViewModel.setErrorMessage(errorMessage);
    }

    // TODO write this later once view is setup (you would just refresh the
    //  page with the commentId object deleted in the database)
    @Override
    public void switchToDeleteCommentView() {

    }
}