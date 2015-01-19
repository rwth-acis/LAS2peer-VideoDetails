
SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;

--
-- Database: `videos`
--

-- --------------------------------------------------------

--
-- Table structure for table `videodetails`
--

CREATE TABLE IF NOT EXISTS `videodetails` (
  `videoId` varchar(250) NOT NULL,
  `url` varchar(250) NOT NULL,
  `thumbnail` varchar(250) NOT NULL,
  `uploader` varchar(50) NOT NULL,
  `tool` varchar(50) NOT NULL,
  `community` varchar(50) NOT NULL,
  `time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `description` varchar(250) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

--
-- Dumping data for table `videodetails`
--

INSERT INTO `videodetails` (`videoId`, `url`, `thumbnail`, `uploader`, `tool`, `community`, `time`, `description`) VALUES
('1', '.localhost', 'http://localhost:81/rest/video/Wildlife.png', 'userBlaBla', 'restservice', 'acis', '2014-12-10 16:14:32', 'This is a test video'),
('2', 'url', 'thumbnail', 'sbakiu', 'restservice', 'acis', '2014-12-04 15:30:39', 'acis meeting'),
('3', 'urlFor3', 'localhost', 'uploaderOf3', 'ahso', 'sse', '2014-12-12 10:35:43', 'This is a video of our course mates'),
('4', 'urlFromTest', 'thumbnail4', 'uploader_New', 'tool', 'community', '2014-12-17 11:22:08', 'description4'),
('6', 'url4', 'thumbnail6', 'uploader7', 'tool', 'community', '2014-12-11 14:09:13', 'description6');

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
