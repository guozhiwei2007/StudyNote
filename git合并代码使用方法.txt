git合并代码使用方法：

1、合并a分支的本地提交记录到b分支；
     1）在a分支上，提交代码到本地；
     2）切换到b分支，拉去最新代码， 然后选中该条提交记录，右键－》Cherry-Pick


2、合并10.3的分支到10.4分支：
     1）git checkout 10.3
     2）git pull —rebase
     3）git checkout -b 10.4
     4）git pull —rebase
     5）git merge 10.3
     6）git status，查看状态，有冲突，解决冲突，
     7）git add .
     8）git commit －m “”
     9）git push
出自：https://www.cnblogs.com/linjiqin/p/7756164.html
