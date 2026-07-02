const fs = require('fs');
const content = fs.readFileSync('app/src/main/java/com/example/presentation/ui/Screens.kt', 'utf8');

let braceCount = 0;
let inString = false;
let escape = false;

let inLineComment = false;
let inBlockComment = false;

let lineNum = 1;
const unbalancedLines = [];

for (let i = 0; i < content.length; i++) {
    const c = content[i];
    const nextC = content[i+1];
    
    if (inString) {
        if (escape) { escape = false; }
        else if (c === '\\') { escape = true; }
        else if (c === '"') { inString = false; }
        if (c === '\n') { lineNum++; }
        continue;
    }
    
    if (inLineComment) {
        if (c === '\n') { inLineComment = false; lineNum++; }
        continue;
    }
    if (inBlockComment) {
        if (c === '*' && nextC === '/') { inBlockComment = false; i++; }
        if (c === '\n') { lineNum++; }
        continue;
    }
    
    if (c === '/' && nextC === '/') { inLineComment = true; i++; continue; }
    if (c === '/' && nextC === '*') { inBlockComment = true; i++; continue; }
    
    // char literals
    if (c === "'" && content[i+2] === "'") { i += 2; continue; }
    
    
    if (c === '"') {
        inString = true;
    } else if (c === '{') {
        braceCount++;
        unbalancedLines.push(lineNum);
    } else if (c === '}') {
        braceCount--;
        unbalancedLines.pop();
    } else if (c === '\n') {
        lineNum++;
    }
}

console.log('Final count:', braceCount);
console.log('Unopened { at lines:', unbalancedLines);
