react－native学习心得

1、left，right，top，bottom必须在绝对布局中才能使用， 且在项目中尽量少用绝对布局。 在做类似弹框那种的时候可以使用；
2、如果父布局使用flexDirection:’row’的时候，子布局必须给设置width，否则子布局不会显示出来；
3、做一个左对齐， 右对齐的布局
    <View flexDirection;’row’>
        <View width;20 height: 20 backgroundColor:’#987654’>
        <View width;20 height: 20 backgroundColor:’#987654’ flex:1>
        <View width;20 height: 20 backgroundColor:’#987654'>
    </View>

4、flexDirection指定的方向为主轴， 与主轴垂直的方向就是次轴。
     alignItems控制子元素在次轴方向的布局， justifyContent控制子元素在主轴方向上的布局。
5、如果父布局使用alignItems布局，子元素使用了alignSelf布局， 则父布局的alignItems属性对子元素没有作用；