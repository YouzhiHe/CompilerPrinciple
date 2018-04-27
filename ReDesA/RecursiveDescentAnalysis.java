package ReDesA;

import com.sun.org.apache.regexp.internal.RE;

import java.util.Scanner;

//设计二：语法分析之递归下降分析法

/**
 * Created by 贺有志 on 2018/4/20.
 */
public class RecursiveDescentAnalysis{

    static int p = 0;
    String code ;
    public RecursiveDescentAnalysis(String code){
        this.code = code;
    }
    public boolean startAnalysis(){

        if(code != null)
         return Fun_E();

        return false;
    }
    //E->TG
    public boolean Fun_E(){
        System.out.println("E->TG");
        Fun_T();
        Fun_G();
        if(code.charAt(p) == '#'){
            return true;
        }
        else{
            return false;
        }
    }
    //T->FS
    public void Fun_T(){
        System.out.println("T->FS");
        Fun_F();
        Fun_S();
    }

    //G->+TG|-TG|ε
    public void Fun_G(){
        if(code.charAt(p) == '+' || code.charAt(p) == '-'){
            String output = "G->"+(code.charAt(p)=='+'?"+":"-")+"TG";
            System.out.println(output);
            p++;
            Fun_T();
            Fun_G();
        }
        else {
            System.out.println("G->ε");
            return;
        }
    }
    //F->(E)|i
    public void Fun_F(){
        if(code.charAt(p)== '('){
            System.out.println("F->(E)");
            p++;
            Fun_E();
        }
        else if(code.charAt(p) == 'i'){
            System.out.println("F->i");
            p++;
        }
        else{
            //Error
            String error = code.substring(0,p)+"分析完成\n"
                    +"index:"+p+"出错";
        }
    }
    //S->*FS|/FS|ε
    public void Fun_S(){
        if(code.charAt(p) == '*' || code.charAt(p) == '/'){
            String output = "S->"+(code.charAt(p)=='*'?"*":"/")+"TG\n";
            p++;
            Fun_F();
            Fun_S();
        }
        else{
            System.out.println("S->ε");
            return;
        }
    }
    public static void main(String[] args){
        String code = new Scanner(System.in).nextLine();
        RecursiveDescentAnalysis RDA =  new RecursiveDescentAnalysis(code);
        boolean flag = RDA.startAnalysis();
        System.out.println(code + (flag?"是":"不是")+"合法符号串");
    }
}
