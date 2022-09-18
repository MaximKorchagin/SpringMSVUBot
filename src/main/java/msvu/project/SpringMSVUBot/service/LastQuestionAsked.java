package msvu.project.SpringMSVUBot.service;

public enum LastQuestionAsked {
    //TODO vvv
    NOTHING("ничего"),
    DB_PRINTED("распечатана бд"),
    WHAT_IS_YOUR_NAME("ваше имя"),
    DB_ADD_REQUEST("добавить в бд");
    public final String label;

    LastQuestionAsked(String label) {
        this.label = label;
    }
}
