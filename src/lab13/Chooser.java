package lab13;

import javax.swing.*;
import java.util.Iterator;
import java.util.Vector;

public class Chooser extends Visitor {
    private Vector<Student> res;
    private Integer semester;

    public Vector<Student> getRes() {
        return res;
    }

    private Vector<String> subjects;
    public Chooser(Integer semester, Vector<String> subjects) {
        this.semester = semester;
        this.subjects = subjects;
        this.res = new Vector<>();
    }

    @Override
    void visit(StudentMarks v) {
        SemesterMarks semesterMarks=v.getSemesterMarks(semester);
        if(semesterMarks==null){
            res.add(v.getStudent());
            return;
        }
        Iterator<String> it=subjects.iterator();
        while(it.hasNext())
            if(semesterMarks.getOrDefault(it.next(),0)<=4){
                res.add(v.getStudent());
                return;
            }
    }
}
