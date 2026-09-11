const path = require('node:path');

module.exports = function configureCoverage(builderOptions) {
  const sourceDirectory = `${path.resolve('src')}${path.sep}`;

  builderOptions.instrumentForCoverage = filename => filename.startsWith(sourceDirectory);

  return {
    name: 'enable-istanbul-coverage',
    setup() {},
  };
};
