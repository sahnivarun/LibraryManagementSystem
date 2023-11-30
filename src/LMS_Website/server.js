const http = require('http');
const fs = require('fs');
const path = require('path');
//axios might have to be used

const port = 20000;

const server = http.createServer((req, res) => {
    // Handling requests
    if (req.method === 'GET' && req.url === '/') {
        // Serve an HTML file
        const filePath = path.join(__dirname, 'login.html');
        fs.readFile(filePath, 'utf8', (err, data) => {
            if (err) {
                res.writeHead(500);
                res.end('Error loading index.html');
            } else {
                res.writeHead(200, { 'Content-Type': 'text/html' });
                res.end(data);
            }
        });
    } else {
        // Handle other requests (e.g., images, stylesheets, scripts)
        const filePath = path.join(__dirname, req.url);
        fs.readFile(filePath, (err, data) => {
            if (err) {
                res.writeHead(404);
                res.end('File not found');
            } else {
                const contentType = getContentType(filePath);
                res.writeHead(200, { 'Content-Type': contentType });
                res.end(data);
            }
        });
    }
});

// Function to determine content type based on file extension
function getContentType(filePath) {
    const extname = path.extname(filePath);
    switch (extname) {
        case '.html':
            return 'text/html';
        case '.css':
            return 'text/css';
        case '.js':
            return 'text/javascript';
        default:
            return 'text/plain';
    }
}

// Start the server
server.listen(port, () => {
    console.log(`Server is running on port ${port}`);
});
