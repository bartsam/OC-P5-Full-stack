const path = require('node:path');

module.exports = builderOptions => {
  const sourceDirectory = `${path.resolve('src')}${path.sep}`;

  builderOptions.instrumentForCoverage = filename => filename.startsWith(sourceDirectory);

  return {
    name: 'enable-istanbul-coverage',
    setup() {},
  };
};
