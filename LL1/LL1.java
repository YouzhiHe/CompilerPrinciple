package LL1;

import java.util.*;

/**
 * 编译原理设计实验4之语法分析值LL(1)分析法的实现
 * Created by 贺有志 on 2018/4/21.
 */
public class LL1 {

    //产生式
    String[][] production;

    //开始符号
    char BeginVn;

    Set<Character> VtSet = new HashSet<>();
    Set<Character> VnSet = new HashSet<>();

    Map<Character,Set<Character>> firstSet = new HashMap<>();
    Map<Character,Set<Character>> followSet = new HashMap<>();

    //LL1表，包含表中内容和索引
    String[][] ll1Table;
    Map<Character,Integer> VnIndex  = new HashMap<>();
    Map<Character,Integer> VtIndex  = new HashMap<>();
    //保存Vn
    Object[] index1;
    //保存Vt
    Object[] index2;


    //只接受不含左递归，且不含公共左公因子的文法
    //规定非终结符为大写字母，其余均为终结符
    public LL1(String[][] production){

        this.production = production;

        if(this.production.length>=1){
            BeginVn = this.production[0][0].charAt(0);
        }
        //统计Vn和Vt的集合，并输出一下
        getVnAndVt();

        System.out.println("Vn：");
        printSet(VnSet);
        System.out.println("Vt：");
        printSet(VtSet);

        //计算First和Follow集合,并输出
        getFirstSetAndFollowSet();
        printFirstAndFollowSet();

        //建立LL(1)分析表，并输出
        createLL1AnalysisTable();
        printLL1AnalysisTable();
    }

    //规定非终结符为大写字母，其余均为终结符
    static boolean isVnChar(char c) {
        if (c >= 'A' && c <= 'Z') {
            return true;
        }
        return false;
    }
    void printSet(Set<Character> set){
        Iterator<Character> it ;
        it = set.iterator();
        while(it.hasNext()){
            System.out.print(it.next()+" ");
        }
        System.out.println();
    }

    void getVnAndVt(){

        int i;
        //统计非终结符（大写字母）和终结符（其它）
        for(i=0;i<production.length;i++){
            //产生式头，必为非终结符
            VnSet.add(production[i][0].charAt(0));
            //产生式尾
            for(int j=0;j<production[i][1].length();j++){
                if(LL1.isVnChar(production[i][1].charAt(j))){
                    VnSet.add(production[i][1].charAt(j));
                }
                else{
                    VtSet.add(production[i][1].charAt(j));
                }
            }
        }
        //初始化集合中的set
        Iterator<Character> it = VnSet.iterator();
        while(it.hasNext()){
            char c = it.next();
            firstSet.put(c,new HashSet<>());
            followSet.put(c,new HashSet<>());
        }
        //检查是否正确

    }

    void putCharToSet(char des,char c,int flag){

        if( flag==1 ){
            Set<Character> set = firstSet.get(des);
            set.add(c);
            firstSet.put(des,set);
        }
        else if( flag==2 ) {
            Set<Character> set = followSet.get(des);
            set.add(c);
            followSet.put(des, set);
        }
    }

    void putSetToFollowSet(char des,Set<Character> set){

        Set<Character> s = followSet.get(des);
        s.addAll(set);
        followSet.put(des,s);
    }


    void getFirstSetAndFollowSet(){

        //计算First和Follow集
        Iterator<Character> it_Vn = VnSet.iterator();

        while(it_Vn.hasNext()){

            //存在重复性计算问题，有待改进
            char c = it_Vn.next();
            getFirstSet(c);
        }
        getFollowSet();
    }
    Set<Character> getFirstSetByString(String s){
        if(!isVnChar(s.charAt(0))){
            Set<Character> result = new HashSet<>();
            result.add(s.charAt(0));
            return result;
        }
        else{
            int i;
            Set<Character> result = new HashSet<>();
            Set<Character> temp = new HashSet<>();
            for(i=0;i<s.length();i++){
                if(isVnChar(s.charAt(i))){

                    //必须使用拷贝构造函数，否则会影响原集合
                    temp = new HashSet<>(firstSet.get(s.charAt(i)));
                    //temp = firstSet.get(s.charAt(i));
                    if(!temp.contains('ε')){
                        //result.containsAll(temp);
                        result.addAll(temp);
                        break;
                    }
                    else{
                        temp.remove('ε');
                        //result.containsAll(temp);
                        result.addAll(temp);
                        continue;
                    }
                }
                else{
                    result.add(s.charAt(i));
                    break;
                }
            }
            //加入ε符
            if(i == s.length()){
                result.add('ε');
            }
            return result;
        }
    }

    int getSizeByHashMap(Map<Character,Set<Character>> set){
        int num = 0;
        Set<Character> keySet = set.keySet();
        Iterator<Character> it = keySet.iterator();
        while(it.hasNext()){
            num += (set.get(it.next())).size();
        }
        return num;
    }

    //未完待续...
    void getFollowSet(){

        //直到每个followSet都不再变化为止,如何判断

        //开始尝试判断集合是否发生了变化，但是不太懂深克隆。于是尝试了下面的方法

        //通过记录集合中元素的总个数，来判断集合是否发生改变

        /*
        for(int i=0;i<production.length;i++){
            System.out.println(production[i][0] + "->"+production[i][1]);
        }

        System.out.println(BeginVn+"'s followSet中"+"加入："+"#");
        */

        //开始符号的followSet加入‘#’
        putCharToSet(BeginVn,'#',2);

        int oldNum = getSizeByHashMap(followSet);

        while(true) {

            for (int i = 0; i < production.length; i++) {

                //System.out.println("In production: "+production[i][0] + "->"+production[i][1]+"\n");

                for(int j = 0;j < production[i][1].length();j++) {

                    if (j != production[i][1].length() - 1) {

                        char c = production[i][1].charAt(j);
                        char cc = production[i][1].charAt(j + 1);

                        if (isVnChar(c)) {

                            if (!isVnChar(cc) /*&& cc != 'ε' */) {
                                putCharToSet(c, cc, 2);

                                //System.out.println(c+"'s followSet中"+"加入："+cc);
                            } /*else if (cc == 'ε') {
                                char pro_head = production[i][0].charAt(0);
                                //getFollowSet(pro_head);
                                putSetToFollowSet(c, followSet.get(pro_head));
                            } */ else {

                                String beta = production[i][1].substring(j + 1, production[i][1].length());
                                Set<Character> set = getFirstSetByString(beta);
                                //System.out.println("beta: "+beta+"'s firstSet");
                                //printSet(set);

                                if (set.contains('ε')) {
                                    char pro_head = production[i][0].charAt(0);
                                    //getFollowSet(pro_head);
                                    //先移除空符
                                    set.remove('ε');
                                    putSetToFollowSet(c, set);
                                    putSetToFollowSet(c, followSet.get(pro_head));

                                    //测试
                                    /*System.out.println(c+"'s followSet中"+"加入：");
                                    printSet(set);
                                    printSet(followSet.get(pro_head));
                                    */

                                } else {
                                    putSetToFollowSet(c, set);

                                    //测试
                                    /*System.out.println(c+"'s followSet中"+"加入：");
                                    printSet(set);
                                    */
                                }
                            }
                        }
                    }
                    else{
                        char c = production[i][1].charAt(j);
                        if(isVnChar(c)) {
                            char pro_head = production[i][0].charAt(0);
                            putSetToFollowSet(c, followSet.get(pro_head));

                            /*System.out.println(c+"'s followSet中"+"加入：");
                            printSet(followSet.get(pro_head));*/
                        }
                    }
                }
            }
            int newNum = getSizeByHashMap(followSet);

            //若集合未发生变化，则结束循环
            if(oldNum == newNum ){
                break;
            }
            oldNum = newNum;
        }
    }

    void getFirstSet(char Vn){

        //遍历产生式
        for(int j=0;j<production.length;j++) {

            if (production[j][0].charAt(0) == Vn) {
                char c = production[j][1].charAt(0);

                //若第一个字符为终结符，则无需考虑后面
                if (!isVnChar(c)) {
                    putCharToSet(Vn, c, 1);
                    continue;
                }
                //否则
                //做个标记，判断是否可以加入空符
                boolean isCanPutSpace = false;

                //遍历产生式尾部
                for (int k = 0; k < production[j][1].length(); k++) {

                    c = production[j][1].charAt(k);
                    if(!isVnChar(c)){
                        putCharToSet(Vn,c,1);
                        break;
                    }
                    //递归求解
                    getFirstSet(c);
                    Set<Character> set = firstSet.get(c);
                    Iterator<Character> it = set.iterator();

                    boolean isContainSpace = false;
                    while (it.hasNext()) {
                        char cc = it.next();
                        //不为空符，并且空符不插入
                        if (cc != 'ε') {
                            putCharToSet(Vn, cc, 1);
                        } else {
                            isContainSpace = true;
                        }
                    }
                    if(!isContainSpace){
                        break;
                    }
                    //若一直判断到最后一个非终结符，且其包含空符，则
                    if(k == production.length -1 ){
                        isCanPutSpace = true;
                    }
                }
                if(isCanPutSpace){
                    putCharToSet(Vn,'ε',1);
                }
            }
        }
    }

    void printFirstAndFollowSet(){

        Set<Character> s1 = firstSet.keySet();
        Set<Character> s2 = followSet.keySet();
        Iterator<Character> it1 = s1.iterator();
        Iterator<Character> it2 = s2.iterator();
        System.out.println("FirstSet：");
        while(it1.hasNext()){
            char c = it1.next();
            System.out.print(c+"'s firstSet：");
            printSet(firstSet.get(c));
        }
        System.out.println("FollowSet：");
        while(it2.hasNext()){
            char c = it2.next();
            System.out.print(c+"'s followSet：");
            printSet(followSet.get(c));
        }
    }

    void printLL1AnalysisTable(){
        System.out.println("LL1分析表："+"\n     ");
        for(int i=0;i<index2.length;i++){
            System.out.print(i+":"+(char)index2[i] + " ");
        }
        System.out.println();
        for(int i=0;i<ll1Table.length;i++){
            System.out.print((char)index1[i]+": ");
            for(int j=0;j<ll1Table[i].length;j++){
                System.out.print(j+":"+ll1Table[i][j]+" ");
            }
            System.out.println();
        }
    }

    //建LL(1)分析表
    void createLL1AnalysisTable(){

        index1 = VnSet.toArray();
        //在Vt中加入#
        VtSet.add('#');
        index2 = VtSet.toArray();

        for(int i=0;i<index1.length;i++){
            VnIndex.put((char)index1[i],i);
        }
        for(int i=0;i<index2.length;i++){
            VtIndex.put((char)index2[i],i);
        }

        //初始化表
        //在建立完成后，若为空串则代表error
        String[][] table = new String[index1.length][index2.length];
        for(int i=0;i<table.length;i++){
            for(int j=0;j<table[i].length;j++){
                table[i][j] = "";
            }
        }

        for(int i=0;i<production.length;i++){

            char pro_head = production[i][0].charAt(0);
            String pro_rear = production[i][1];
            Set<Character> set = getFirstSetByString(pro_rear);
            Iterator<Character> it = set.iterator();
            while(it.hasNext()){
                char c = it.next();
                if(c != 'ε') {
                    table[VnIndex.get(pro_head)][VtIndex.get(c)] = production[i][1];
                }
            }

            //若包含ε，则加入follow集
            if(set.contains('ε')){
                set = followSet.get(pro_head);
                it = set.iterator();
                while(it.hasNext()){
                    table[VnIndex.get(pro_head)][VtIndex.get(it.next())] = production[i][1];
                }
            }
        }

        ll1Table = table;

    }

    void startAnalysis(String s) {

        boolean isLegal ;

        Stack<Character> stack = new Stack<>();
        //初始化栈
        stack.push('#');
        stack.push(BeginVn);
        StringBuffer print = new StringBuffer();
        print.append("步骤  分析栈 剩余输入串 所用产生式\n");
        int step = 0;
        int p = 0;
        while (true) {
            //步骤
            print.append(step+" ");

            char c = s.charAt(p);
            if (stack.peek() == '#' && c == '#') {
                print.append("匹配完成\n");
                isLegal = true;
                break;
            } else if (stack.peek() == c) {
                print.append("栈顶符号: "+c+"与输入符号一致。逐出栈顶，读入下一符号\n");
                stack.pop();
                p++;
                step++;
            } else if (isVnChar(stack.peek())) {

                String table = ll1Table[VnIndex.get(stack.peek())][VtIndex.get(c)];
                String analStack = stack.toString();
                String laterString = s.substring(p,s.length());
                char  pro_head = stack.peek();
                String pro_rear = table;

                //无法匹配
                if (table.equals("")) {
                    print.append("栈顶与输入不匹配！栈顶："+stack.peek()+",输入："+c+"。分析结束！\n");
                    isLegal = false;
                    break;
                }
                //匹配成功
                else {
                    step++;
                    print.append(analStack+" "+laterString+" "+pro_head+"->"+pro_rear+"\n");
                    stack.pop();

                    //空符不入栈
                    if(table.length() == 1 && table.charAt(0)=='ε'){
                        continue;
                    }
                    //逆序压栈
                    for (int i = table.length() - 1; i >= 0; i--) {
                        stack.push(table.charAt(i));
                    }
                }
            }
        }
        if(isLegal){
            System.out.println(print.toString());
            System.out.println(s+"为合法符号串");
        }
        else{
            System.out.println(s+"为非法符号串");
        }
    }


    public static void main(String[] args){


        System.out.println("请先输入产生式数量，然后输入产生式内容");
        Scanner sc = new Scanner(System.in);
        ArrayList<String> textgram = new ArrayList<>();
        int n = sc.nextInt();
        //去掉回车键
        sc.nextLine();
        //读入产生式
        for(int i=0;i<n;i++){
            String s = sc.nextLine();
            textgram.add(s);
        }
        String[][] production;
        production = new String[textgram.size()][2];
        Iterator<String> it = textgram.iterator();
        int i=0;
        while(it.hasNext()){
            String[] s = it.next().split("->");
            production[i][0] = s[0];
            production[i][1] = s[1];
            i++;
        }

        LL1 ll1 = new LL1(production);
        System.out.println("请输入一以#结束的符号串(包括+—*/（）i#):");
        String input = sc.nextLine();
        ll1.startAnalysis(input);
    }
}
