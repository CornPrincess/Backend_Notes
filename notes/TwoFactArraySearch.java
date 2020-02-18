public class TwoFactArraySearch {
    public boolean Find(int target, int[][] array) {
        if (array == null || array.length < 1) {
            return false;
        }

        for (int i = 0; i < array.length; i++) {
            for (int j = 0; j < array[i].length; j++) {
                if (array[i][j] == target) {
                    return true;
                }
            }
        }
        return false;
    }

    // 从右上角开始找
    public boolean Find2(int target, int[][] array) {
        if (array == null || array.length < 1) {
            return false;
        }

        int rows = array.length;
        int columns = array[0].length;
        int c = columns - 1;
        int r = 0;

        while (r <= rows-1 && c >= 0) {
            if(array[r][c] == target) {
                return true;
            } else if(target > array[r][c]) {
                r++;
                continue;
            } else {
                c--;
                continue;
            }
        }

        return false;
    }
}
