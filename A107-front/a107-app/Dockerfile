FROM node:16.19.0-alpine as builder
WORKDIR "/app"
COPY package.json .
RUN npm install --force
COPY . .
ENV NODE_ENV production
RUN npm run build

FROM nginx
RUN mkdir /app
WORKDIR /app
RUN rm /etc/nginx/conf.d/default.conf
COPY ./nginx.conf /etc/nginx/conf.d
COPY --from=builder /app/build /app/build
EXPOSE 3000
CMD ["nginx", "-g", "daemon off;"]

