package LL1;

import java.util.*;

/**
 * 编译原理设计4-语法分析值LL(1)分析法的实现
 * * 根据某一文法编制调试LL（1）分析程序，以便对任意输入的符号串进行分析。本次实验的目的主要是加深对预测分析LL（1）分析法的理解。
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


    //只接受不含左递归，且不含公共左公因子的文法
    public LL1(String[][] production){
        this.production = production;
        if(this.production.length>=1){
            BeginVn = this.production[0][0].charAt(0);
        }
        //统计Vn和Vt的集合
        getVnAndVt();
        //计算First和Follow集合
        getFirstSetAndFollowSet();
        //建立LL(1)分析表
        createLL1Table();
    }

    static boolean isBigChar(char c) {
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

    void print(){
        printSet(VtSet);
        printSet(VnSet);
    }

    Set<Character> getFirstSetByString(String s){
        if(!isBigChar(s.charAt(0))){
            Set<Character> result = new HashSet<>();
            result.add(s.charAt(0));
            return result;
        }
        else{
            int i;
            Set<Character> result = new HashSet<>();
            Set<Character> temp = new HashSet<>();
            for(i=0;i<s.length();i++){
                if(isBigChar(s.charAt(i))){
                    temp = firstSet.get(s.charAt(i));
                    result.containsAll(temp);
                    if(!temp.contains('ε')){
                        break;
                    }
                    else{
                        continue;
                    }
                }
                else{
                    result.add(s.charAt(i));
                    break;
                }
            }
            return result;
        }
    }

    void getVnAndVt(){

        int i;
        //统计非终结符（大写字母）和终结符（其它）
        for(i=0;i<production.length;i++){
            //产生式头，必为非终结符
            VnSet.add(production[i][0].charAt(0));
            //产生式尾
            for(int j=0;j<production[i][1].length();j++){
                if(LL1.isBigChar(production[i][1].charAt(j))){
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
            getFollowSet(c);
        }
    }


    //未完待续...
    void getFollowSet(char Vn){

        //开始符号的followSet加入‘#’
        putCharToSet(BeginVn,'#',2);

        //直到每个followSet都不再变化为止,如何判断
        Map<Character,Set<Character>> oldFollowSet = new HashMap<>(followSet);

        while(true) {
            /* equal
                Integer i1 = 1;
                Integer i2 = 1;
                if(i1.equals(i2)){
                    System.out.println("equal");
                }*/

            //判断是否有变化
            Set<Character> it1 = oldFollowSet.keySet();
            Set<Character> it2 = followSet.keySet();

            Iterator<Character> it3 = it1.iterator();
            Iterator<Character> it4 = it2.iterator();
            boolean isChange = false;
            while(it3.hasNext() && it4.hasNext()){
                Set<Character> set1 = oldFollowSet.get(it3.next());
                Set<Character> set2 = oldFollowSet.get(it4.next());

                if(!(set1.containsAll(set2) && set2.containsAll(set1))){
                    isChange = true;
                    break;
                }
            }
            //若未发生改变则无需继续循环
            if(!isChange){
                break;
            }

            for (int i = 0; i < production.length; i++) {
                for(int j=0;j<production[i][1].length() - 1;j++){
                    char c = production[i][1].charAt(j);
                    char cc = production[i][1].charAt(j+1);
                    if(isBigChar(c)){
                        if(!isBigChar(cc)){
                            putCharToSet(c,cc,2);
                        }
                        else{
                            int k;
                            for(k=j+1;k<production[i][1].length();k++) {
                                //去掉空符再加入followSet
                                Set<Character> set = firstSet.get(cc);
                                //不包含空符加入非终结符之后直接结束
                                if (!set.contains('ε')) {
                                    putSetToFollowSet(c, set);
                                    break;
                                } else {
                                    set.remove('ε');
                                    putSetToFollowSet(c, set);
                                    continue;
                                }
                            }
                            if(k==production[i][1].length()){
                                //follow(b) = follow(b) + follow(production_head);
                                char ccc = production[i][0].charAt(0);
                                getFollowSet(ccc);
                                putSetToFollowSet(c,followSet.get(ccc));
                            }
                        }
                    }
                }
            }
        }
    }

    void getFirstSet(char Vn){

        //遍历产生式
        for(int j=0;j<production.length;j++) {

            if (production[j][0].charAt(0) == Vn) {
                char c = production[j][1].charAt(0);

                //若第一个字符为终结符，则无需考虑后面
                if (!isBigChar(c)) {
                    putCharToSet(Vn, c, 1);
                    continue;
                }
                //否则
                //做个标记，判断是否可以加入空符
                boolean isCanPutSpace = false;

                //遍历产生式尾部
                for (int k = 0; k < production[j][1].length(); k++) {

                    c = production[j][1].charAt(k);
                    if(!isBigChar(c)){
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

    //建LL(1)分析表
    void createLL1Table(){

        Object[] Vn = VnSet.toArray();
        Object[] Vt = VtSet.toArray();

        for(int i=0;i<Vn.length;i++){
            VnIndex.put((char)Vn[i],i);
        }
        for(int i=0;i<Vt.length;i++){
            VtIndex.put((char)Vt[i],i);
        }


        //初始化表
        //在建立完成后，若为空串则代表error
        String[][] table = new String[Vn.length][Vt.length];
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
                isLegal = true;
                break;
            } else if (stack.peek() == c) {
                stack.pop();
                p++;
            } else if (isBigChar(stack.peek())) {

                String table = ll1Table[VnIndex.get(stack.peek())][VtIndex.get(c)];
                String analStack = stack.toString();
                String laterString = s.substring(p,s.length());
                char  pro_head = stack.peek();
                String pro_rear = table;

                //无法匹配
                if (table.equals("")) {
                    isLegal = false;
                    break;
                }
                //匹配成功
                else {
                    print.append(analStack+" "+laterString+" "+pro_head+pro_rear+"\n");
                    stack.pop();
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

        Scanner sc = new Scanner(System.in);
        ArrayList<String> textgram = new ArrayList<>();
        int n = sc.nextInt();

        for(int i=0;i<n;i++){
            textgram.add(sc.nextLine());
        }

        String[][] production;
        production = new String[textgram.size()][2];
        Iterator<String> it = textgram.iterator();
        int i=0;
        while(it.hasNext()){
            String[] s = it.next().split("->");
            System.out.println(s[0]);
            production[i][0] = s[0];
            production[i][0] = s[1];
            i++;
        }

        LL1 ll1 = new LL1(production);
        System.out.println("输入一以#结束的符号串(包括+—*/（）i#)：\n");
        String input = sc.nextLine();
        ll1.startAnalysis(input);
    }
}
