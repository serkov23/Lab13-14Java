package lab13;

import java.util.Comparator;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Vector;

class Student implements Comparable<Student> {
    private int bookNumber;
    private String name;

    public Student() {
    }

    public Student(int bookNumber, String name) {
        this.bookNumber = bookNumber;
        this.name = name;
    }

    public int getBookNumber() {
        return bookNumber;
    }

    public void setBookNumber(int bookNumber) {
        this.bookNumber = bookNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Student o) {
        int tmp = name.compareTo(o.name);
        return tmp == 0 ? bookNumber - o.bookNumber : tmp;
    }

    @Override
    public String toString() {
        return Integer.toString(bookNumber) + " " + name;
    }
}

public class StudentMarks {
    private Student student;
    private TreeMap<Integer, SemesterMarks> marks;

    public TreeMap<Integer, SemesterMarks> getMarks() {
        return marks;
    }

    public StudentMarks() {
    }
    public static CmpSemester getCmp(int sem, Vector<String>subj){
        return new CmpSemester(sem, subj);
    }
    public StudentMarks(Student student, TreeMap<Integer, SemesterMarks> marks) {
        this.student = student;
        this.marks = marks;
    }

    public void setMarks(TreeMap<Integer, SemesterMarks> marks) {
        this.marks = marks;
    }

    @Override
    public String toString() {
        return student.toString();
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    SemesterMarks getSemesterMarks(Integer semesterNumber) {
        return marks.getOrDefault(semesterNumber, null);
    }

    void accept(Visitor visitor) {
        visitor.visit(this);
    }

    public static class CmpSemester implements Comparator<StudentMarks> {
        Integer semesterNumber;
        Vector<String> subjects;

        public CmpSemester(int semesterNumber, Vector<String> subjects) {
            this.semesterNumber = semesterNumber;
            this.subjects = subjects;

        }

        @Override
        public int compare(StudentMarks o1, StudentMarks o2) {
            if (!o1.marks.containsKey(semesterNumber) && !o2.marks.containsKey(semesterNumber))
                return o1.student.compareTo(o2.student);
            if (!o1.marks.containsKey(semesterNumber))
                return 1;
            if (!o2.marks.containsKey(semesterNumber))
                return -1;
            if (subjects.isEmpty())
                return o1.student.compareTo(o2.student);
            int sum1 = 0, sum2 = 0;
            var marks1 = o1.marks.get(semesterNumber);
            var marks2 = o2.marks.get(semesterNumber);
            Iterator<String> it = subjects.iterator();
            while (it.hasNext()) {
                var cur = it.next();
                sum1 += marks1.getOrDefault(cur, 0);
                sum2 += marks2.getOrDefault(cur, 0);
            }
            return (sum1 - sum2) == 0 ? o1.student.compareTo(o2.student) : (sum1 - sum2);
        }
    }

}

class SemesterMarks extends TreeMap<String, Integer> {
    public SemesterMarks() {
        super();
    }
}