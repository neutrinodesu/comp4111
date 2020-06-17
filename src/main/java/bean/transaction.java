package bean;


import com.fasterxml.jackson.annotation.JsonProperty;

public class transaction {

    @JsonProperty("Transaction")
    private String transaction;

    @JsonProperty("Book")
    private String id;

    @JsonProperty("Action")
    private String action;

    @JsonProperty("Operation")
    private String operation;

    public String getTransaction() {
        return transaction;
    }

    public void setTransaction(String transaction) {
        this.transaction = transaction;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getOperation() {
        return operation;
    }

    public void setOperation(String operation) {
        this.operation = operation;
    }


}
