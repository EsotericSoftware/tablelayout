Ragel.exe -G2 -J -V -o temp.dot TableLayoutParser.rl
dot -Tpng temp.dot -o TableLayoutParser.png
pause
