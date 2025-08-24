server {
    server_name spacefarm.shop;

         location /ws-chat {
          # Spring Boot 백엔드 서버 주소
                proxy_pass http://localhost:8080/ws-chat;

                # 👇 웹소켓 연결을 위한 필수 헤더 설정 👇
                proxy_set_header Upgrade $http_upgrade;
                proxy_set_header Connection "Upgrade";

                # 기존에 사용하시던 표준 프록시 헤더들
                proxy_set_header Host $host;
                proxy_set_header X-Real-IP $remote_addr;
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;                                                                                                                                                        
                proxy_set_header X-Forwarded-Proto $scheme;                                                                                                                                                                         
        }                                                                                                                                                                                                                           
        location / {                                                                                                                                                                                                                
                proxy_pass http://localhost:8080/;                                                                                                                                                                                  
                proxy_set_header Host $host;                                                                                                                                                                                        
                proxy_set_header X-Real-IP $remote_addr;                                                                                                                                                                            
                proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;                                                                                                                                                        
                proxy_set_header X-Forwarded-Proto $scheme;                                                                                                                                                                         
                proxy_set_header Authorization $http_authorization;                                                                                                                                                                 
                                                                                                                                                                                                                                    
                if ($request_method = 'OPTIONS') {                                                                                                                                                                                  
                   add_header 'Access-Control-Allow-Origin' $cors_origin always;                                                                                                                                                    
                   add_header 'Access-Control-Allow-Methods' 'GET, POST, PUT, DELETE, OPTIONS'; 
                  add_header 'Access-Control-Allow-Headers' 'Authorization,Content-Type';                                                                                                                                          
                   add_header 'Access-Control-Allow-Credentials' 'true';                                                                                                                                                            
                    return 204; # 204 No Content 응답으로 즉시 종료                                                                                                                                                                 
                }                                                                                                                                                                                                                   
                                                                                                                                                                                                                                    
        }                                                                                                                                                                                                                           
                                                                                                                                                                                                                                    
    listen 443 ssl; # managed by Certbot                                                                                                                                                                                            
    ssl_certificate /etc/letsencrypt/live/spacefarm.shop/fullchain.pem; # managed by Certbot                                                                                                                                        
    ssl_certificate_key /etc/letsencrypt/live/spacefarm.shop/privkey.pem; # managed by Certbot                                                                                                                                      
    include /etc/letsencrypt/options-ssl-nginx.conf; # managed by Certbot                                                                                                                                                           
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem; # managed by Certbot                                                                                                                                                             

}       
server {                                                                                                                                                                                                                            
    if ($host = spacefarm.shop) {                                                                                                                                                                                                   
    return 301 https://$host$request_uri;                                                                                                                                                                                       
    } # managed by Certbot

        listen 80;                                                                                                                                                                                                                  
        server_name spacefarm.shop;                                                                                                                                                                                                 
    return 404; # managed by Certbot
}       
                         