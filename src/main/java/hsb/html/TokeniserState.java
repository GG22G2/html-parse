package hsb.html;




public enum TokeniserState {
    Data {
        // start代表字符开始位置，可以是空格等，
        public void read(byte[] htmlBytes, int start, int[] constructIndex, int constructPosition) {
            int index = constructIndex[constructPosition++];
            byte c = htmlBytes[index];
            switch (c) {
                case '<':
                    byte next = htmlBytes[index + 1];
                    if (next == '!') {
                        //代表文档开始 或者frame开始
                        //或者是注解 <!--

                    } else if (next == '/') {
                        //代表标签闭合
                    } else if (next == '?') {

                    } else if ((next >= 'A' && next <= 'Z') || (next >= 'a' && next <= 'z')) {
                        //跳转到标签开始
                        start = index;
                        // TagStart;
                    }
                    //判断开始标签和 start之间是否有字符

                    if (index > start) { //当作文本标签处理
                        String text = new String(htmlBytes, start, index - start);
                    }
                    break;
                case '/':
                    //因为现在没有标记 </ 这样的结构，
                    byte lastC = htmlBytes[index - 1];
                    if (lastC=='<'){
                        //标签结束

                    }

                    break;
                default:
                    //找到下一个开始标签或闭合标签 < ，然后把中间的数据当作字符处理

                    int strStart = start+1;
                    int strEnd;
                    do {
                        strEnd = constructIndex[constructPosition++];
                    } while (htmlBytes[strEnd] != '<');


                    constructPosition--;
                    //Data
                    break;
            }
        }
    },
    TagStart {
        // start代表<位置
        public void read(byte[] htmlBytes, int start, int[] constructIndex, int constructPosition) {
            int index = constructIndex[constructPosition++];
            byte c = htmlBytes[index];

            String tagName;
            switch (c) {
                case '>':
                    //闭合 TagEnd
                    tagName = new String(htmlBytes, start, index - start);

                    break;
                case '/':
                    //自闭合 TagSelfEnd
                    tagName = new String(htmlBytes, start, index - start);

                    break;
                default:
                    //当前index是标签名称的最后一个字符的位置索引
                    start++;
                    tagName = new String(htmlBytes, start, index - start + 1);

                    // AttributeOrTagEnd;
                    break;
            }
        }
    },
    AttributeOrTagEnd {
        // start值不做要求   , 这个可以改成一个函数更好一点
        public void read(byte[] htmlBytes, int start, int[] constructIndex, int constructPosition) {
            int index = constructIndex[constructPosition++];
            byte c = htmlBytes[index];
            switch (c) {
                case '>':
                    //闭合 TagEnd

                    break;
                case '/':
                    //自闭合 TagSelfEnd

                    break;
                default:
                    //当前index是属性名称的第一个字符的索引
                    start = index;
                    // AttributeNameStart
                    break;
            }
        }
    },
    AttributeNameStart {
        // start代表属性名称的第一个字符的位置
        public void read(byte[] htmlBytes, int start, int[] constructIndex, int constructPosition) {
            int index = constructIndex[constructPosition++];
            byte c = htmlBytes[index];
            String attrName;
            switch (c) {
                case '=':
                    attrName = new String(htmlBytes, start, index - start);
                    // AttributeEqualSymbol

                    break;
                case '>':
                    //闭合 TagEnd
                    attrName = new String(htmlBytes, start, index - start);

                    break;
                case '/':
                    //自闭合 TagSelfEnd
                    attrName = new String(htmlBytes, start, index - start);

                    break;

                default:
                    //字符，这个字符是当前属性名称的结尾字符，index代表结尾字符的位置
                    attrName = new String(htmlBytes, start, index - start + 1);
                    // CurAttrVOrNextAttrN

                    break;
            }

        }
    },
    CurAttrVOrNextAttrN {
        // start值不做要求
        public void read(byte[] htmlBytes, int start, int[] constructIndex, int constructPosition) {
            int index = constructIndex[constructPosition++];
            byte c = htmlBytes[index];
            switch (c) {
                case '=':
                    //当前属性的等号
                    start = index;
                    //  AttributeEqualSymbol
                    break;
                case '>':
                    //闭合 TagEnd
                    break;
                case '/':
                    //自闭合 TagSelfEnd
                    break;
                default:
                    //字符，下一个属性名称的开始
                    // AttributeNameStart

                    break;
            }
        }
    },
    AttributeEqualSymbol {
        //start代表当前 = 的索引位置
        public void read(byte[] htmlBytes, int start, int[] constructIndex, int constructPosition) {
            int index = constructIndex[constructPosition++];
            byte c = htmlBytes[index];
            String attrValue;


            //这里解析不对 应该先判断等号后一个元素是不是空格
            switch (c) {
                case '\"':
                    //以双引号开始的字符串，找下一个双引号

                    break;
                case '\'':
                    //以单引号开始的字符串，找下一个单引号

                    break;
                case '>':
                    //类似 <div id= ></div> <div id=></div> ,需要处理一下空属性名
                    //TagEnd
                    break;
                case '/':
                    //如果是等号后的/, 即：'=/' ,当作字符处理
                    if (index - start == 1) {   //当作字符处理,并且是属性值的第一个字符
                        attrValue = consumeNoQuoteString(htmlBytes, index, constructIndex, constructPosition);

                    } else { //当作自闭和标签处理
                        //TagSelfEnd
                    }
                    break;

                default:
                    //等号后边没有跟 “ 或者 ' ，也不是>,所有有两种可能的结构 <div id=abcd class></div> 或 <div id= class></div>
                    //1.等号后的下一个位置是空字符(\r \n \t等)，index 代表的是下一个属性名开始的位置索引
                    //2.等号后的下一个位置是字符时(a-z,A-Z)，index 代表的是当前属性值结束位置索引,或是字符串中间是除了>之外的任意结构字符(< = " ' / !)的位置索引
                    byte next = htmlBytes[start + 1];
                    if (next != '\0' && next != '\t' && next != '\n' && next != '\f' && next != '\r' && next != ' ') {
                        //只要不是空格类字符，index 就都当作属性值的第一个字符位置, 切只有遇到 > 或 空字符 时才认为字符串结束
                        //下一个索引只能是 结构字符位置或者空字符左测位置
                        attrValue = consumeNoQuoteString(htmlBytes, index, constructIndex, constructPosition);


                    } else {
                        //index当作下一个属性的开始位置
                        // AttributeNameStart

                    }
                    break;
            }
        }
    },
    TagEnd {
        public void read(byte[] htmlBytes, int start, int[] constructIndex, int constructPosition) {


        }
    },
    TagSelfEnd {
        public void read(byte[] htmlBytes, int start, int[] constructIndex, int constructPosition) {


        }
    };

    public abstract void read(byte[] htmlBytes, int start, int[] constructIndex, int constructPosition);


    public String consumeDoubleQuoteString() {

        return null;
    }

    public String consumeSingleQuoteString() {

        return null;
    }

    public String consumeNoQuoteString(byte[] htmlBytes, int strStartIndex, int[] constructIndex, int constructPosition) {
        String attrValue = "";
        while (constructPosition < constructIndex.length) {
            int strEndIndex = constructIndex[constructPosition++];
            byte what = htmlBytes[strEndIndex];
            byte whatNext = htmlBytes[strEndIndex + 1];
            if (what == '>') {
                //标签结束,上一个位置是字符串结束
                attrValue = new String(htmlBytes, strStartIndex, strEndIndex - strStartIndex);
                //跳转到结束标签 TagEnd

                break;
            } else if (whatNext == '\0' || whatNext == '\t' || whatNext == '\n' || whatNext == '\f' || whatNext == '\r' || whatNext == ' ') {
                attrValue = new String(htmlBytes, strStartIndex, strEndIndex - strStartIndex + 1);
                //跳转到 AttributeOrTagEnd

                break;
            }
        }
        return attrValue;
    }


}
