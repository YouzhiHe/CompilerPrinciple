package lexerTest;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by 贺有志 on 2018/4/8.
 */

//词法分析器
public class Lexer {

    public String code = "";
    public int index = 0;
    public int line = 1;
    public Lexer(String code){
        this.code = code;
    }
    public ArrayList<Token> listToken = new ArrayList<>();
    public ArrayList<ErrorToken> listErrorToken = new ArrayList<>();

    public void printToken(){

        Iterator<Token> it1 = listToken.iterator();
        while(it1.hasNext()){
            System.out.println(it1.next());
        }
        Iterator<ErrorToken> it2 = listErrorToken.iterator();
        while(it2.hasNext()){
            System.out.println(it2.next());
        }
    }

    boolean isHavaNext(){
        if(index<=code.length()-1){
            return true;
        }
        return false;
    }
    void startLexAnalysis(){

        while(isHavaNext()){
            getSpace();
            if(!isHavaNext()){
                break;
            }
            getIdentifier();
            if(!isHavaNext()){
                break;
            }
            getRealNumber();
            if(!isHavaNext()){
                break;
            }
            getOperator();
            if(!isHavaNext()){
                break;
            }
            getOtherGap();
        }

    }
    void getSpace(){
        if(Util.isSpace(code.charAt(index))){
            if(code.charAt(index) == '\n' /*|| code.charAt(index) == '\r'*/){
                line++;
            }
            index++;
        }
    }
    void getIdentifier() {
        if (Util.isAlpha(code.charAt(index))) {
            int begin = index++;//index加1
            int status = 1;
            while (true) {
                //System.out.println("in while");
                switch (status) {
                    case 1:
                        if (Util.isAlphaOrNumber(code.charAt(index))) {
                            index++;
                            status = 1;
                        } else {
                            status = 2;
                        }
                        break;
                    case 2:
                        String value = code.substring(begin, index);
                        int tag = 2;
                        if (Util.isReserverWord(value)) {
                            tag = 1;
                        }
                        Token token = new Token(tag, value);
                        listToken.add(token);
                        return;
                }
            }
        }
    }
    void getRealNumber(){
        if(Util.isNumber(code.charAt(index))){
            int begin = index++;//index加1
            int status = 1;
            while(true) {
                switch (status) {
                    case 1:
                        if (Util.isNumber(code.charAt(index))) {
                            index++;
                            status = 1;
                            continue;
                        }
                        if (code.charAt(index) == '.') {
                            index++;
                            status = 2;
                            continue;
                        }
                        if (code.charAt(index) == 'E') {
                            index++;
                            status = 4;
                            continue;
                        }
                        String value = code.substring(begin, index);
                        listToken.add(new Token(3, value));//实数类型码为3
                        return;//接受状态
                    case 2:
                        if (Util.isNumber(code.charAt(index))) {
                            index++;
                            status = 3;
                            continue;
                        }
                        else{
                            //error
                            listErrorToken.add(new ErrorToken(code.substring(begin,index),line));
                            //listToken.add(new Token(6,code.substring(begin,index)));
                            return;
                        }
                    case 3:
                        if (Util.isNumber(code.charAt(index))) {
                            index++;
                            status = 3;
                            continue;
                        }
                        if(code.charAt(index)=='E'){
                            index++;
                            status = 4;
                            continue;
                        }
                        listErrorToken.add(new ErrorToken(code.substring(begin,index),line));
                        //listToken.add(new Token(3, code.substring(begin, index)));//实数类型码为3
                        return;
                    case 4:
                        if(Util.isNumber(code.charAt(index))){
                            index++;
                            status = 5;
                            continue;
                        }
                        else{
                            //error
                            listErrorToken.add(new ErrorToken(code.substring(begin,index),line));
                            //listToken.add(new Token(3, code.substring(begin, index)));//实数类型码为3
                            return;
                        }
                    case 5:
                        if(Util.isNumber(code.charAt(index))){
                            index++;
                            status = 5;
                            continue;
                        }
                        listToken.add(new Token(3, code.substring(begin, index)));//实数类型码为3
                        return;
                }
            }
        }
    }
    void getOperator(){
        if(Util.isOperator(code.charAt(index))){

            if(Util.isSingleOperator(code.charAt(index))){
                listToken.add(new Token(4,code.charAt(index++)+""));
            }

            if(code.charAt(index) == '!'){
                if(code.charAt(index+1)=='='){
                    index = index+2;
                    listToken.add(new Token(4,"!="));
                }
                else{
                    //error
                    listErrorToken.add(new ErrorToken(code.charAt(index++)+"",line));
                    //listToken.add(new Token(6,code.charAt(index++)+""));
                    return;
                }
            }
            else if(code.charAt(index) == '<' || code.charAt(index) == '>'){
                if(code.charAt(index+1)=='='){
                    listToken.add(new Token(4,code.charAt(index)+"="));
                    index = index+2;
                }
                else{
                    listToken.add(new Token(4,code.charAt(index++)+""));
                }
            }
        }
    }
    void getOtherGap(){
        if(Util.isGap(code.charAt(index))){
            listToken.add(new Token(5,code.charAt(index++)+""));//index加1
        }
    }

    public  static String readToString(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try {
            FileInputStream in = new FileInputStream(file);
            in.read(filecontent);
            in.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            return new String(filecontent, encoding);
        } catch (UnsupportedEncodingException e) {
            System.err.println("The OS does not support " + encoding);
            e.printStackTrace();
            return null;
        }
    }

    public static void main(String[] args){
        String code = Lexer.readToString("C:\\Users\\贺有志\\Desktop\\code.txt");
        System.out.println(code);
        if(code != null) {
            Lexer lexer = new Lexer(code);
            lexer.startLexAnalysis();
            lexer.printToken();
        }
    }
}

//单词
class Token{
    public int tag;
    public String value;
    public Token(int tag,String value){
        this.tag = tag;
        this.value = value;
    }
    @Override
    public String toString(){
        return "("+tag+","+value+")";
    }
}
class ErrorToken extends Token{
    public int line;
    public ErrorToken(String value,int line){
        super(6,value);
        this.line = line;
    }
    @Override
    public String toString(){
        return "Error in line "+line+":"+"("+tag+","+value+")";
    }
}

class ReserverWord{
    public static String[] reserverWord = {
            "int","for","while","if","do",
            "return","break","continue"
    };
}

//工具类
class Util{

    public static boolean isSpace(char c){
        if(c == ' ' || c == '\t' || c == '\n' || c == '\r'){
            return true;
        }
        return false;
    }
    public static boolean isAlpha(char c){
        if( (c >= 'A' && c <= 'Z') || (c >= 'a' && c<= 'z') ){
            return true;
        }
        return false;
    }
    public static boolean isAlphaOrNumber(char c){
        if(isAlpha(c)){
            return true;
        }
        if(c >= '0' && c<= '9'){
            return true;
        }
        return false;
    }
    public static boolean isReserverWord(String s){
        for(int i=0;i<ReserverWord.reserverWord.length;i++){
            if( s.equals(ReserverWord.reserverWord[i])){
                return true;
            }
        }
        return false;
    }
    public static boolean isNumber(char c){
        if( c == '0' || c == '1' || c == '2' || c == '3' ||  c == '4'||
                c == '5' || c == '6' || c == '7' || c == '8' || c == '9'){
            return true;
        }
        return false;
    }
    public static boolean isOperator(char c){
        if( c== '+' || c== '-'|| c=='*'|| c=='/'
                || c== '='|| c=='>' || c=='<' || c=='!'){
            return true;
        }
        return false;
    }

    public static boolean isSingleOperator(char c){
        if( c== '+' || c== '-'|| c=='*'|| c=='/'
                || c== '='){
            return true;
        }
        return false;
    }
    public static boolean isGap(char c){
        if ( c==',' || c==';' || c=='{'|| c=='}'|| c=='('|| c==')'){
            return true;
        }
        return false;
    }
}
