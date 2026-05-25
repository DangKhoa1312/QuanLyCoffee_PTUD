@echo off
echo Dang nap du lieu vao database QuanLyQuanCaPhe...
sqlcmd -S localhost -U sa -P 123456 -d QuanLyQuanCaPhe -i "cafe_insert_data_v2.sql"
echo Nap du lieu hoan tat!
pause
