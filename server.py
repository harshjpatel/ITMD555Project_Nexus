from http.server import HTTPServer, BaseHTTPRequestHandler
import urllib.parse

class MockServer(BaseHTTPRequestHandler):
    def do_GET(self):
        self.send_response(200)
        self.send_header("Content-type", "text/html")
        self.end_headers()

        # Parse URL and query parameters
        parsed_path = urllib.parse.urlparse(self.path)
        params = urllib.parse.parse_qs(parsed_path.query)

        post_id = parsed_path.path.split('/')[-1]
        title = params.get('title', ['Post Title'])[0]
        content = params.get('content', ['Post content goes here...'])[0]
        author = params.get('author', ['Anonymous'])[0]

        html = f"""
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1">
            <title>Nexus - {title}</title>
            <style>
                body {{
                    font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, Helvetica, Arial, sans-serif;
                    background-color: #0F111A;
                    color: #E0E7FF;
                    margin: 0;
                    padding: 20px;
                    display: flex;
                    justify-content: center;
                }}
                .card {{
                    background-color: #1A1D29;
                    border-radius: 16px;
                    padding: 24px;
                    box-shadow: 0 10px 25px rgba(0,0,0,0.5);
                    max-width: 500px;
                    width: 100%;
                    border: 1px solid #2D3748;
                }}
                .header {{
                    display: flex;
                    align-items: center;
                    margin-bottom: 20px;
                }}
                .avatar {{
                    width: 40px;
                    height: 40px;
                    border-radius: 50%;
                    background: linear-gradient(135deg, #6C7BFF, #3B82F6);
                    margin-right: 12px;
                }}
                .title {{
                    font-size: 22px;
                    font-weight: 800;
                    margin-bottom: 16px;
                    color: #FFFFFF;
                    line-height: 1.3;
                }}
                .content {{
                    font-size: 16px;
                    line-height: 1.6;
                    color: #CBD5E0;
                    white-space: pre-wrap;
                }}
                .divider {{
                    height: 1px;
                    background-color: #2D3748;
                    margin: 24px 0;
                }}
                .actions {{
                    display: flex;
                    justify-content: space-around;
                    color: #718096;
                    font-weight: 600;
                    font-size: 14px;
                }}
                .action-item {{
                    display: flex;
                    align-items: center;
                    gap: 6px;
                }}
                .badge {{
                    background-color: #2D3748;
                    color: #6C7BFF;
                    padding: 4px 12px;
                    border-radius: 20px;
                    font-size: 12px;
                    margin-bottom: 8px;
                    display: inline-block;
                }}
            </style>
        </head>
        <body>
            <div class="card">
                <div class="header">
                    <div class="avatar"></div>
                    <div>
                        <div style="font-weight: 700; color: #FFFFFF;">{author}</div>
                        <div style="font-size: 12px; color: #718096;">Posted to Nexus • Just now</div>
                    </div>
                </div>
                <div class="badge">Post ID: {post_id}</div>
                <div class="title">{title}</div>
                <div class="content">{content}</div>
                <div class="divider"></div>
                <div class="actions">
                    <div class="action-item"><span>▲</span> Upvote</div>
                    <div class="action-item"><span>▼</span> Downvote</div>
                    <div class="action-item"><span>💬</span> Comment</div>
                    <div class="action-item"><span>🔗</span> Share</div>
                </div>
            </div>
        </body>
        </html>
        """
        self.wfile.write(bytes(html, "utf8"))

print("Server starting on http://localhost:8080...")
print("Make sure to run 'adb reverse tcp:8080 tcp:8080' to connect your device.")
HTTPServer(('0.0.0.0', 8080), MockServer).serve_forever()
