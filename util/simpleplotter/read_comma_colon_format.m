function [X] = read_comma_colon_format(str)

A  = strread(str,'%s','delimiter',',');
n1 = length(A);
temp  = strread(A{1},'%s','delimiter',':');
n2 = length(temp);
clear temp

X = nan(n1,n2);

for i=1:n1
    B = strread(A{i},'%s','delimiter',':');
    for j=1:n2
        X(i,j) = str2double(B{j});
    end
end