package com.hdweiss.morgand.data.dao;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@DatabaseTable(tableName = "OrgAgendas")
public class OrgAgenda {

    @DatabaseField(generatedId = true)
    public int Id;

    public static final String TITLE_FIELD_NAME = "title";
    @DatabaseField(columnName = TITLE_FIELD_NAME)
    public String title;

    @DatabaseField
    public String files;

    @DatabaseField
    public String tags;

    @DatabaseField
    public String priorities;

    @DatabaseField
    public String todos;

    @DatabaseField
    public boolean includeHabits;

    @DatabaseField
    public boolean includeInactiveTodos;


    public List<OrgNode> getNodes() {
        try {
            return OrgNodeRepository.queryBuilder().query();
        } catch (SQLException e) {
            e.printStackTrace();
            return new ArrayList<OrgNode>();
        }
    }
}
