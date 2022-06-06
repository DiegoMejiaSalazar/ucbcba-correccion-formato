module.exports = {
  type: "react-component",
  babel: {
    cherryPick: "lodash"
  },
  npm: {
    esModules: false,
    umd: false
  },
  webpack: {
    html: {
      template: 'demo/src/index.html'
    }
  },
  devServer: {
    proxy: {
      '/api': 'http://127.0.0.1:8080/'
    }
  }
};
