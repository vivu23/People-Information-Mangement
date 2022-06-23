package mvc.model;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FetchResults {
    private long numRows;

    private int pageSize;

    private int currentPage;

    private List<Person> people;

    //accessors

    public FetchResults(int numRows, int pageSize, int currentPage, List<Person> people) {
        this.numRows = numRows;
        this.pageSize = pageSize;
        this.currentPage = currentPage;
        this.people = people;
    }

    public FetchResults() {
    }

    public static FetchResults fromJSONObject(JSONObject json) {
        try {
            List<Person> people = new ArrayList<>();
            for (Object person : json.getJSONArray("people")) {
                people.add(Person.fromJSONObject((JSONObject) person));
            }
            FetchResults fetchResults = new FetchResults(json.getInt("numRows"), json.getInt("pageSize"), json.getInt("currentPage"), people);
            return fetchResults;
        } catch(Exception e) {
            throw new IllegalArgumentException("Unable to parse person from provided json: " + json.toString());
        }
    }


    public long getNumRows() {
        return numRows;
    }

    public void setNumRows(long numRows) {
        this.numRows = numRows;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public List<Person> getPeople() {
        return people;
    }

    public void setPeople(List<Person> people) {
        this.people = people;
    }
}
