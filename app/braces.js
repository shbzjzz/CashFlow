const fs = require('fs');
const content = fs.readFileSync('app/src/main/java/com/example/presentation/ui/Screens.kt', 'utf8');

let braceCount = 0;
let inString = false;
let escape = false;

let lineNum = 1;
const unbalancedLines = [];

for (let i = 0; i < content.length; i++) {
    const c = content[i];
    
    // Simplistic Kotlin comment handling needed:
    // Let's just assume no braces in comments or ignore it. 
    // Actually, let's skip strings to be safe:
    if (inString) {
        if (escape) { escape = false; }
        else if (c === '\\') { escape = true; }
        else if (c === '"') { inString = false; }
        if (c === '\n') { lineNum++; }
        continue;
    }
    
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
